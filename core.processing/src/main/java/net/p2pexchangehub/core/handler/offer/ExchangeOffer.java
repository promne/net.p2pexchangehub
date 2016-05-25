package net.p2pexchangehub.core.handler.offer;

import java.math.BigDecimal;
import java.util.Arrays;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.offer.OfferCanceledEvent;
import net.p2pexchangehub.core.api.offer.OfferCreatedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditDeclineConfirmedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditDeclineReservedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditRejectedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditedEvent;
import net.p2pexchangehub.core.api.offer.OfferDebitConfirmedEvent;
import net.p2pexchangehub.core.api.offer.OfferDebitRequestedEvent;
import net.p2pexchangehub.core.api.offer.OfferExchangeCompletedEvent;
import net.p2pexchangehub.core.api.offer.OfferMatchedEvent;

public class ExchangeOffer extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String userAccountId;

    private String currencyOffered;

    private BigDecimal amountOfferedMin;

    private BigDecimal amountOfferedMax;

    // how much was actually offered in the deal
    private BigDecimal amountOffered;

    private String currencyRequested;

    private BigDecimal amountRequestedExchangeRate;

    // how much will the user actually get
    private BigDecimal amountRequested;

    private OfferState state;

    private String matchedExchangeOfferId;

    public ExchangeOffer() {
        super();
    }

    public String getId() {
        return id;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getCurrencyOffered() {
        return currencyOffered;
    }

    public BigDecimal getAmountOfferedMin() {
        return amountOfferedMin;
    }

    public BigDecimal getAmountOfferedMax() {
        return amountOfferedMax;
    }

    public BigDecimal getAmountOffered() {
        return amountOffered;
    }

    public String getCurrencyRequested() {
        return currencyRequested;
    }

    public BigDecimal getAmountRequestedExchangeRate() {
        return amountRequestedExchangeRate;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public OfferState getState() {
        return state;
    }

    public String getMatchedExchangeOfferId() {
        return matchedExchangeOfferId;
    }

    public ExchangeOffer(String offerId, String userAccountId, String currencyOffered, BigDecimal amountOfferedMin, BigDecimal amountOfferedMax, String currencyRequested,
            BigDecimal requestedExchangeRate) {
        if (amountOfferedMin.compareTo(amountOfferedMax)>0) {
            throw new IllegalArgumentException("Minimal amount can't be bigger than maximal amount");
        }
        if (currencyOffered.equalsIgnoreCase(currencyRequested)) {
            throw new IllegalArgumentException("Can't request the same currency that is offered");            
        }
        apply(new OfferCreatedEvent(offerId, userAccountId, currencyOffered, amountOfferedMin, amountOfferedMax, currencyRequested, requestedExchangeRate));
    }

    @EventHandler
    private void handleCreated(OfferCreatedEvent event) {
        this.currencyOffered = event.getCurrencyOffered();
        this.currencyRequested = event.getCurrencyRequested();
        this.id = event.getOfferId();
        this.userAccountId = event.getUserAccountId();
        this.amountOfferedMax = event.getAmountOfferedMax();
        this.amountOfferedMin = event.getAmountOfferedMin();
        this.amountRequestedExchangeRate = event.getRequestedExchangeRate();
        this.state = OfferState.UNPAIRED;
    }
    
    public void matchWithOffer(String matchedOfferId, BigDecimal amountOffered, BigDecimal amountRequested) {
        if (this.state != OfferState.UNPAIRED) {
            throw new IllegalStateException(String.format("Unable to match offer %s in state %s", this.id, this.state));
        }
        if (amountOffered.compareTo(this.amountOfferedMin) < 0 || amountOffered.compareTo(this.amountOfferedMax) > 0) {
            throw new IllegalStateException(String.format("Offered amount %s is out of the offer %s - %s range", amountOffered, this.amountOfferedMin, this.amountOfferedMax));
        }
        if (amountOffered.multiply(this.amountRequestedExchangeRate).compareTo(amountRequested)>0) {
            throw new IllegalStateException(String.format("Reqeusted exchange of %s for %s doesn't match exchange rate %s of offer %s", amountOffered, amountRequested, this.amountRequestedExchangeRate, this.id));            
        }
        
        apply(new OfferMatchedEvent(this.id, matchedOfferId, amountOffered, amountRequested));
    }
    
    @EventHandler
    private void handleMatched(OfferMatchedEvent event) {
        this.amountOffered = event.getAmountOffered();
        this.amountRequested = event.getAmountRequested();
        this.matchedExchangeOfferId = event.getMatchedOfferId();
        this.state = OfferState.WAITING_FOR_PAYMENT;
    }

    public void credit(String transactionId, String userAccountId, CurrencyAmount amount) {
        if (state != OfferState.WAITING_FOR_PAYMENT || !amount.getCurrencyCode().equals(currencyOffered) || amount.getAmount().compareTo(amountOffered)!=0) {
            apply(new OfferCreditRejectedEvent(id, transactionId, userAccountId, amount));        
        }        
        apply(new OfferCreditedEvent(id, transactionId, userAccountId, amount));        
    }
    
    @EventHandler
    private void handleCredited(OfferCreditedEvent event) {
        this.state = OfferState.PAYED;
    }

    @EventHandler
    private void handleCreditRejected(OfferCreditRejectedEvent event) {
        //nothing, keeps state to waiting for payment
    }
    
    public void reserveCreditDecline(String transactionId) {
        if (state != OfferState.PAYED) {
            throw new IllegalStateException(String.format("Unable to reserve credit decline for offer %s with state %s", id, state));
        }        
        apply(new OfferCreditDeclineReservedEvent(id, transactionId, userAccountId));        
    }
    
    @EventHandler
    private void handleCreditDeclineReserved(OfferCreditDeclineReservedEvent event) {
        this.state = OfferState.CREDIT_DECLINE_REQUESTED;
    }
    
    public void confirmCreditDecline(String transactionId) {
        if (state != OfferState.CREDIT_DECLINE_REQUESTED) {
            throw new IllegalStateException(String.format("Unable to confirm charge for offer %s with state %s", id, state));
        }        
        apply(new OfferCreditDeclineConfirmedEvent(id, transactionId, userAccountId));
    }
    
    @EventHandler
    private void handleDischargeConfirmed(OfferCreditDeclineConfirmedEvent event) {
        this.state = OfferState.WAITING_FOR_PAYMENT;
    }

    public void completeExchange() {
        apply(new OfferExchangeCompletedEvent(id));
    }

    @EventHandler
    private void handleExchangeCompleted(OfferExchangeCompletedEvent event) {
        this.state = OfferState.EXCHANGE_COMPLETE;
    }
    
    public void requestDebit() {
        if (state != OfferState.EXCHANGE_COMPLETE) {
            throw new IllegalStateException(String.format("Unable to accept request payment for offer %s with state %s", id, state));
        }        
        apply(new OfferDebitRequestedEvent(this.id));
    }

    @EventHandler
    private void handleDebitRequested(OfferDebitRequestedEvent event) {
        this.state = OfferState.DEBIT_REQUESTED;
    }

    public void confirmDebit() {
        if (state != OfferState.DEBIT_REQUESTED) {
            throw new IllegalStateException(String.format("Unable to accept request payment for offer %s with state %s", id, state));
        }        
        apply(new OfferDebitConfirmedEvent(this.id));
    }

    @EventHandler
    private void handleCreditConfirmed(OfferDebitConfirmedEvent event) {
        this.state = OfferState.CLOSED;
    }
    
    public void cancel() {
        if (!Arrays.asList(OfferState.UNPAIRED).contains(state)) {
            throw new IllegalStateException(String.format("Unable to cancel offer %s with state %s", id, state));            
        }
        apply(new OfferCanceledEvent(id));
    }

    @EventHandler
    private void handleCanceled(OfferCanceledEvent event) {
        state = OfferState.CANCELED;
    }

    
}
