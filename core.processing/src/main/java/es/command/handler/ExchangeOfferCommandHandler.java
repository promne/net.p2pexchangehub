package es.command.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;

import es.aggregate.ExchangeOffer;
import es.aggregate.ExternalBankAccount;
import es.aggregate.TestBankAccount;
import es.aggregate.UserAccount;
import es.command.CancelExchangeOfferCommand;
import es.command.CreateOfferCommand;
import es.command.MatchExchangeOfferCommand;
import es.command.RequestOfferPaymentCommand;
import es.command.SetOwnerAccountNumberForOfferCommand;
import esw.domain.BankAccount;
import esw.view.BankAccountView;
import esw.view.OfferView;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.offer.OfferState;
import george.test.exchange.core.processing.service.CurrencyService;
import george.test.exchange.core.processing.service.bank.BankProvider;
import george.test.exchange.core.processing.service.bank.BankProviderException;

@Singleton
public class ExchangeOfferCommandHandler {

    @Inject
    private CurrencyService currencyService;
    
    @Inject
    private OfferView offerView;

    @Inject
    @Any
    private Instance<BankProvider> bankProviders;
    
    @Inject
    private Repository<UserAccount> userAccountRepository;
    
    @Inject
    private Repository<ExchangeOffer> repository;

    @Inject
    private Repository<TestBankAccount> repositoryAccounts;
    
    @Inject
    private BankAccountView accountsView;    

    @Inject
    private Logger log;
    
    public Repository<ExchangeOffer> getRepository() {
        return repository;
    }
    
    public void setRepository(Repository<ExchangeOffer> repository) {
        this.repository = repository;
    }

    private Optional<BankProvider> getBankProvider(ExternalBankType bankType) {
        return StreamSupport.stream(bankProviders.spliterator(), false).filter(p -> p.getType()==bankType).findFirst();
    }
    
    @CommandHandler
    public void handleCreateOffer(CreateOfferCommand command) {
        userAccountRepository.load(command.getUserAccountId()); //ensure user account exists
        ExchangeOffer offer = new ExchangeOffer(command.getOfferId(), command.getUserAccountId(), command.getCurrencyOffered(), command.getAmountOfferedMin(), command.getAmountOfferedMax(), command.getCurrencyRequested(), command.getRequestedExchangeRate());
        repository.add(offer);
    }

    @CommandHandler
    public void handleMatchOffers(MatchExchangeOfferCommand command) {
        userAccountRepository.load(command.getUserAccountId()); //ensure user account exists
        ExchangeOffer matchOffer = repository.load(command.getMatchOfferId());
        
        if (matchOffer.getUserAccountId().equals(command.getUserAccountId())) {
            throw new IllegalStateException("Unable to match offers for the same user account");
        }
        
        //create fitting counter offer 
        BigDecimal requestedAmountExchanged = currencyService.calculateExchangePay(command.getAmountRequested(), matchOffer.getCurrencyOffered(), matchOffer.getCurrencyRequested(), matchOffer.getAmountRequestedExchangeRate());
        if (requestedAmountExchanged.compareTo(command.getAmountOffered()) > 0) {
            throw new IllegalStateException("Unable to match offer with lower bid");            
        }
        
        BigDecimal offerExchangeRate = command.getAmountRequested().divide(command.getAmountOffered(), 4, RoundingMode.DOWN);
        ExchangeOffer offer = new ExchangeOffer(command.getNewOfferId(), command.getUserAccountId(), matchOffer.getCurrencyRequested(), command.getAmountOffered(), command.getAmountOffered(), matchOffer.getCurrencyOffered(), offerExchangeRate);
        repository.add(offer);
        
        //and match with minimal amounts satisfying both
        matchOffer.matchWithOffer(command.getNewOfferId(), command.getAmountRequested(), requestedAmountExchanged, offerView.generateUniqueReferenceId(matchOffer.getCurrencyOffered()));
        offer.matchWithOffer(command.getMatchOfferId(), command.getAmountOffered(), command.getAmountRequested(), offerView.generateUniqueReferenceId(offer.getCurrencyOffered()));
        
        List<BankAccount> activeAccountsOffered = accountsView.listActiveAccounts(matchOffer.getCurrencyOffered());
        if (activeAccountsOffered.isEmpty()) {
            throw new IllegalStateException("Unable to find active account for currency " + matchOffer.getCurrencyOffered());
        }
        matchOffer.setIncomingPaymentExternalAccount(activeAccountsOffered.get(0).getId());

        List<BankAccount> activeAccountsRequested = accountsView.listActiveAccounts(matchOffer.getCurrencyRequested());
        if (activeAccountsRequested.isEmpty()) {
            throw new IllegalStateException("Unable to find active account for currency " + matchOffer.getCurrencyRequested());
        }
        offer.setIncomingPaymentExternalAccount(activeAccountsRequested.get(0).getId());
    }
    
    @CommandHandler
    public void handleRequestPayment(RequestOfferPaymentCommand command) throws BankProviderException {
        ExchangeOffer offer = repository.load(command.getOfferId());
        ExchangeOffer matchedOffer = repository.load(offer.getMatchedExchangeOfferId());
        if (offer.getState() != OfferState.PAYMENT_RECEIVED) {
            throw new IllegalStateException(String.format("Unable to pay offer %s without receiving money first", command.getOfferId()));
        }
        if (!Arrays.asList(OfferState.PAYMENT_RECEIVED, OfferState.SEND_MONEY_REQUESTED, OfferState.CLOSED).contains(matchedOffer.getState())) {
            throw new IllegalStateException(String.format("Unable to pay offer %s when matching offer %s is in state %s", command.getOfferId(), matchedOffer.getId(), matchedOffer.getState()));
        }
        if (offer.getOwnerAccountNumber()==null) {
            throw new IllegalStateException(String.format("Unable to pay offer %s without recipients account", command.getOfferId(), matchedOffer.getId(), matchedOffer.getState()));            
        }
        
        Optional<BankAccount> bankAccount = accountsView.getBankAccount(matchedOffer.getIncomingExternalBankAccountId());
        if (!bankAccount.isPresent()) {
            throw new IllegalStateException(String.format("There is no bank account available to payoff offer %s", command.getOfferId()));            
        }
        
        Optional<BankProvider> bankProvider = getBankProvider(bankAccount.get().getBankType());

        ExternalBankAccount bankAccountAggregate = repositoryAccounts.load(bankAccount.get().getId());
        
        TransactionRequestExternal transactionRequest = new TransactionRequestExternal();
        transactionRequest.setBankAccount(bankAccountAggregate);
        transactionRequest.setAmount(offer.getAmountRequested());
        transactionRequest.setDetailInfo(String.format("%s %s %s rate %s", offer.getReferenceId(), offer.getAmountOffered(), offer.getCurrencyOffered(), offer.getAmountRequestedExchangeRate()));
        transactionRequest.setRecipientAccountNumber(offer.getOwnerAccountNumber());
        
        bankProvider.get().processTransactionRequest(transactionRequest);
        offer.requestPayment();
    }    

    @CommandHandler
    public void handleSetOwnerAccountNumberForOfferCommand(SetOwnerAccountNumberForOfferCommand command) {
        ExchangeOffer offer = repository.load(command.getOfferId());
        offer.setOwnerAccountNumber(command.getAccountNumber());        
    }

    @CommandHandler
    public void handleCancelExchangeOfferCommand(CancelExchangeOfferCommand command) {
        repository.load(command.getOfferId()).cancel();        
    }
    
}
