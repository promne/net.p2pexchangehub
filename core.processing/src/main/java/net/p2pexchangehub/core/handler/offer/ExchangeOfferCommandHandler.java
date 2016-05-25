package net.p2pexchangehub.core.handler.offer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;

import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.processing.service.CurrencyService;
import george.test.exchange.core.processing.service.bank.BankProvider;
import george.test.exchange.core.processing.service.bank.BankProviderException;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.offer.CancelExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.CompleteOfferExchangeCommand;
import net.p2pexchangehub.core.api.offer.ConfirmOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.CreditOfferCommand;
import net.p2pexchangehub.core.api.offer.MatchExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.RequestOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.RequestOfferDebitCommand;
import net.p2pexchangehub.core.handler.external.bank.TestBankAccount;
import net.p2pexchangehub.core.handler.user.UserAccount;
import net.p2pexchangehub.view.repository.BankAccountRepository;

@Singleton
public class ExchangeOfferCommandHandler {

    @Inject
    private CurrencyService currencyService;
    
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
    private BankAccountRepository bankAccountRepository;    

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
        matchOffer.matchWithOffer(command.getNewOfferId(), command.getAmountRequested(), requestedAmountExchanged);
        offer.matchWithOffer(command.getMatchOfferId(), command.getAmountOffered(), command.getAmountRequested());
    }

    @CommandHandler
    public void handleCredit(CreditOfferCommand command) {
        repository.load(command.getOfferId()).credit(command.getTransactionId(), command.getUserAccountId(), command.getAmount());
    }

    @CommandHandler
    public void handleCreditDeclineRequest(RequestOfferCreditDeclineCommand command) throws BankProviderException {
        repository.load(command.getOfferId()).reserveCreditDecline(command.getTransactionId());
    }    

    @CommandHandler
    public void handleCreditDeclineConfirmed(ConfirmOfferCreditDeclineCommand command) {
        repository.load(command.getOfferId()).confirmCreditDecline(command.getTransactionId());        
    }
    
    @CommandHandler
    public void handleCompleteExchange(CompleteOfferExchangeCommand command) {
        ExchangeOffer offer = repository.load(command.getOfferId());
        ExchangeOffer matchedOffer = repository.load(offer.getMatchedExchangeOfferId());
        if (offer.getState() != OfferState.PAYED) {
            throw new IllegalStateException(String.format("Unable to pay offer %s without receiving money first", command.getOfferId()));
        }
        if (!Arrays.asList(OfferState.PAYED, OfferState.EXCHANGE_COMPLETE).contains(matchedOffer.getState())) {
            throw new IllegalStateException(String.format("Unable to pay offer %s when matching offer %s is in state %s", command.getOfferId(), matchedOffer.getId(), matchedOffer.getState()));
        }
        //TODO: locking ?
        offer.completeExchange();
    }
    
    @CommandHandler
    public void handleRequestDebit(RequestOfferDebitCommand command) throws BankProviderException {
        ExchangeOffer offer = repository.load(command.getOfferId());
        UserAccount userAccount = userAccountRepository.load(offer.getUserAccountId());
        
        //TODO split to multiple steps
        offer.requestDebit();
        userAccount.chargeFromOffer(offer.getId(), new CurrencyAmount(offer.getCurrencyRequested(),offer.getAmountRequested()));
        offer.confirmDebit();
    }    
    
    @CommandHandler
    public void handleCancelExchangeOfferCommand(CancelExchangeOfferCommand command) {
        repository.load(command.getOfferId()).cancel();        
    }
    
}
