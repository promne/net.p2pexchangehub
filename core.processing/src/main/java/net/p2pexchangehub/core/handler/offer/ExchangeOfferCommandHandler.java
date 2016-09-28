package net.p2pexchangehub.core.handler.offer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Currency;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.MetaData;
import org.axonframework.repository.Repository;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.offer.CancelExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.CompleteOfferExchangeCommand;
import net.p2pexchangehub.core.api.offer.ConfirmOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.CreditOfferCommand;
import net.p2pexchangehub.core.api.offer.MatchExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.RequestOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.RequestOfferDebitCommand;
import net.p2pexchangehub.core.api.offer.UnmatchExchangeOfferCommand;
import net.p2pexchangehub.core.handler.user.UserAccount;
import net.p2pexchangehub.core.processing.service.bank.BankProviderException;
import net.p2pexchangehub.core.util.ExchangeRateEvaluator;

@Singleton
public class ExchangeOfferCommandHandler {

    @Inject
    private Repository<UserAccount> userAccountRepository;
    
    @Inject
    private Repository<ExchangeOffer> repository;

    @Inject
    private ExchangeRateEvaluator exchangeRateEvaluator;
    
    public Repository<ExchangeOffer> getRepository() {
        return repository;
    }
    
    public void setRepository(Repository<ExchangeOffer> repository) {
        this.repository = repository;
    }

    @CommandHandler
    public void handleCreateOffer(CreateOfferCommand command, MetaData metadata) {
        userAccountRepository.load(command.getUserAccountId()); //ensure user account exists
        ExchangeOffer offer = new ExchangeOffer(command.getOfferId(), command.getUserAccountId(), command.getCurrencyOffered(), command.getAmountOfferedMin(), command.getAmountOfferedMax(), command.getCurrencyRequested(), command.getRequestedExchangeRateExpression(), metadata);
        repository.add(offer);
    }

    @CommandHandler
    public void handleMatchOffers(MatchExchangeOfferCommand command, MetaData metadata) {
        userAccountRepository.load(command.getUserAccountId()); //ensure user account exists
        ExchangeOffer matchOffer = repository.load(command.getMatchOfferId());
        
        if (matchOffer.getUserAccountId().equals(command.getUserAccountId())) {
            throw new IllegalStateException("Unable to match offers for the same user account");
        }
        
        BigDecimal matchOfferRequestedExchangeRate = exchangeRateEvaluator.evaluate(matchOffer.getRequestedExchangeRateExpression());
        
        //create fitting counter offer 
        BigDecimal requestedAmountExchanged = exchangeRateEvaluator.calculateExchangePay(command.getAmountRequested(), matchOffer.getCurrencyOffered(), matchOffer.getCurrencyRequested(), matchOffer.getRequestedExchangeRateExpression());
        if (requestedAmountExchanged.compareTo(command.getAmountOffered()) > 0) {
            throw new IllegalStateException("Unable to match offer with lower bid");            
        }
        
        BigDecimal offerExchangeRate = exchangeRateEvaluator.calculateRateRounded(command.getAmountOffered(), command.getAmountRequested()); 

        //lazy thinker check to be sure
        if (command.getAmountRequested().multiply(matchOfferRequestedExchangeRate).setScale(Currency.getInstance(matchOffer.getCurrencyOffered()).getDefaultFractionDigits(), RoundingMode.HALF_UP).compareTo(requestedAmountExchanged)>0) {
          throw new IllegalStateException(command.toString());            
        }
        if (command.getAmountOffered().multiply(offerExchangeRate).setScale(Currency.getInstance(matchOffer.getCurrencyRequested()).getDefaultFractionDigits(), RoundingMode.HALF_UP).compareTo(command.getAmountRequested())>0) {
            throw new IllegalStateException(command.toString());            
        }
        
        ExchangeOffer offer = new ExchangeOffer(command.getNewOfferId(), command.getUserAccountId(), matchOffer.getCurrencyRequested(), command.getAmountOffered(), command.getAmountOffered(), matchOffer.getCurrencyOffered(), offerExchangeRate.toPlainString(), metadata);
        repository.add(offer);
        //and match with minimal amounts satisfying both
        matchOffer.matchWithOffer(command.getNewOfferId(), command.getAmountRequested(), requestedAmountExchanged, metadata);
        offer.matchWithOffer(command.getMatchOfferId(), command.getAmountOffered(), command.getAmountRequested(), metadata);
    }

    @CommandHandler
    public void handle(UnmatchExchangeOfferCommand command, MetaData metadata) {
        ExchangeOffer offer1 = repository.load(command.getOfferId());
        ExchangeOffer offer2 = repository.load(offer1.getMatchedExchangeOfferId());
        
        //TODO: this is extremely nasty
        offer1.unmatchOffer(metadata);
        offer2.unmatchOffer(metadata);
    }
    
    @CommandHandler
    public void handleCredit(CreditOfferCommand command, MetaData metadata) {
        repository.load(command.getOfferId()).credit(command.getTransactionId(), command.getUserAccountId(), command.getAmount(), metadata);
    }

    @CommandHandler
    public void handleCreditDeclineRequest(RequestOfferCreditDeclineCommand command, MetaData metadata) throws BankProviderException {
        repository.load(command.getOfferId()).reserveCreditDecline(command.getTransactionId(), metadata);
    }    

    @CommandHandler
    public void handleCreditDeclineConfirmed(ConfirmOfferCreditDeclineCommand command, MetaData metadata) {
        repository.load(command.getOfferId()).confirmCreditDecline(command.getTransactionId(), metadata);        
    }
    
    @CommandHandler
    public void handleCompleteExchange(CompleteOfferExchangeCommand command, MetaData metadata) {
        ExchangeOffer offer = repository.load(command.getOfferId());
        ExchangeOffer matchedOffer = repository.load(offer.getMatchedExchangeOfferId());
        if (offer.getState() != OfferState.PAYED) {
            throw new IllegalStateException(String.format("Unable to pay offer %s without receiving money first", command.getOfferId()));
        }
        if (!Arrays.asList(OfferState.PAYED, OfferState.EXCHANGE_COMPLETE).contains(matchedOffer.getState())) {
            throw new IllegalStateException(String.format("Unable to pay offer %s when matching offer %s is in state %s", command.getOfferId(), matchedOffer.getId(), matchedOffer.getState()));
        }
        //TODO: locking ?
        offer.completeExchange(metadata);
    }
    
    @CommandHandler
    public void handleRequestDebit(RequestOfferDebitCommand command, MetaData metadata) throws BankProviderException {
        ExchangeOffer offer = repository.load(command.getOfferId());
        UserAccount userAccount = userAccountRepository.load(offer.getUserAccountId());
        
        //TODO split to multiple steps
        offer.requestDebit(metadata);
        userAccount.chargeFromOffer(offer.getId(), new CurrencyAmount(offer.getCurrencyRequested(),offer.getAmountRequested()), metadata);
        offer.confirmDebit(metadata);
    }    
    
    @CommandHandler
    public void handleCancelExchangeOfferCommand(CancelExchangeOfferCommand command, MetaData metadata) {
        repository.load(command.getOfferId()).cancel(metadata);        
    }
    
}
