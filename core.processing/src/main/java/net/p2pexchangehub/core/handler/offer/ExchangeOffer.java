package net.p2pexchangehub.core.handler.offer;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.axonframework.domain.MetaData;
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
import net.p2pexchangehub.core.api.offer.OfferRequestedAmountChangedEvent;
import net.p2pexchangehub.core.api.offer.OfferUnmatchedEvent;

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

    private String requestedExchangeRateExpression;

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

    public String getRequestedExchangeRateExpression() {
        return requestedExchangeRateExpression;
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

    public ExchangeOffer(@NotNull String offerId, @NotNull String userAccountId, @NotNull String currencyOffered, @NotNull BigDecimal amountOfferedMin, @NotNull BigDecimal amountOfferedMax, @NotNull String currencyRequested,
            @NotNull String requestedExchangeRateExpression, MetaData metaData) {
        if (amountOfferedMin.compareTo(amountOfferedMax)>0) {
            throw new IllegalArgumentException("Minimal amount can't be bigger than maximal amount");
        }
        if (currencyOffered.equalsIgnoreCase(currencyRequested)) {
            throw new IllegalArgumentException("Can't request the same currency that is offered");            
        }
        apply(new OfferCreatedEvent(offerId, userAccountId, currencyOffered, currencyRequested), metaData);
        apply(new OfferRequestedAmountChangedEvent(offerId, amountOfferedMin, amountOfferedMax, requestedExchangeRateExpression));
    }

    @EventHandler
    private void handleCreated(OfferCreatedEvent event) {
        this.currencyOffered = event.getCurrencyOffered();
        this.currencyRequested = event.getCurrencyRequested();
        this.id = event.getOfferId();
        this.userAccountId = event.getUserAccountId();
        this.state = OfferState.UNPAIRED;
    }
    
    @EventHandler
    private void handleRequestedAmountChanged(OfferRequestedAmountChangedEvent event) {
        this.amountOfferedMax = event.getAmountOfferedMax();
        this.amountOfferedMin = event.getAmountOfferedMin();
        this.requestedExchangeRateExpression = event.getExchangeRateExpression();
    }
    
    public void unmatchOffer(MetaData metaData) {
        if (this.state != OfferState.WAITING_FOR_PAYMENT) {
            throw new IllegalStateException(String.format("Unable to unmatch offer %s in state %s", this.id, this.state));
        }
        apply(new OfferUnmatchedEvent(this.id, matchedExchangeOfferId), metaData);
    }
    
    @EventHandler
    private void handleUnmatched(OfferUnmatchedEvent event) {
        this.amountOffered = null;
        this.amountRequested = null;
        this.matchedExchangeOfferId = null;
        this.state = OfferState.UNPAIRED;
    }
    
    public void matchWithOffer(String matchedOfferId, BigDecimal amountOffered, BigDecimal amountRequested, MetaData metaData) {
        if (this.state != OfferState.UNPAIRED) {
            throw new IllegalStateException(String.format("Unable to match offer %s in state %s", this.id, this.state));
        }
        if (amountOffered.compareTo(this.amountOfferedMin) < 0 || amountOffered.compareTo(this.amountOfferedMax) > 0) {
            throw new IllegalStateException(String.format("Offered amount %s is out of the offer %s - %s range", amountOffered, this.amountOfferedMin, this.amountOfferedMax));
        }
        //TODO add validation
//        if (amountOffered.multiply(this.amountRequestedExchangeRate).compareTo(amountRequested)>0) {
//            throw new IllegalStateException(String.format("Reqeusted exchange of %s for %s doesn't match exchange rate %s of offer %s", amountOffered, amountRequested, this.amountRequestedExchangeRate, this.id));            
//        }
        
        apply(new OfferMatchedEvent(this.id, matchedOfferId, amountOffered, amountRequested), metaData);
    }
    
    @EventHandler
    private void handleMatched(OfferMatchedEvent event) {
        this.amountOffered = event.getAmountOffered();
        this.amountRequested = event.getAmountRequested();
        this.matchedExchangeOfferId = event.getMatchedOfferId();
        this.state = OfferState.WAITING_FOR_PAYMENT;
    }

    public void credit(String transactionId, String userAccountId, CurrencyAmount amount, MetaData metaData) {
        if (state != OfferState.WAITING_FOR_PAYMENT || !amount.getCurrencyCode().equals(currencyOffered) || amount.getAmount().compareTo(amountOffered)!=0) {
            apply(new OfferCreditRejectedEvent(id, transactionId, userAccountId, amount), metaData);        
        } else {        
            apply(new OfferCreditedEvent(id, transactionId, userAccountId, amount), metaData);
        }
    }
    
    @EventHandler
    private void handleCredited(OfferCreditedEvent event) {
        this.state = OfferState.PAYED;
    }

    @EventHandler
    private void handleCreditRejected(OfferCreditRejectedEvent event) {
        //nothing, keeps state to waiting for payment
    }
    
    public void reserveCreditDecline(String transactionId, MetaData metaData) {
        if (state != OfferState.PAYED) {
            throw new IllegalStateException(String.format("Unable to reserve credit decline for offer %s with state %s", id, state));
        }        
        apply(new OfferCreditDeclineReservedEvent(id, transactionId, userAccountId), metaData);        
    }
    
    @EventHandler
    private void handleCreditDeclineReserved(OfferCreditDeclineReservedEvent event) {
        this.state = OfferState.CREDIT_DECLINE_REQUESTED;
    }
    
    public void confirmCreditDecline(String transactionId, MetaData metaData) {
        if (state != OfferState.CREDIT_DECLINE_REQUESTED) {
            throw new IllegalStateException(String.format("Unable to confirm charge for offer %s with state %s", id, state));
        }        
        apply(new OfferCreditDeclineConfirmedEvent(id, transactionId, userAccountId), metaData);
    }
    
    @EventHandler
    private void handleDischargeConfirmed(OfferCreditDeclineConfirmedEvent event) {
        this.state = OfferState.WAITING_FOR_PAYMENT;
    }

    public void completeExchange(MetaData metaData) {
        apply(new OfferExchangeCompletedEvent(id), metaData);
    }

    @EventHandler
    private void handleExchangeCompleted(OfferExchangeCompletedEvent event) {
        this.state = OfferState.EXCHANGE_COMPLETE;
    }
    
    public void requestDebit(MetaData metaData) {
        if (state != OfferState.EXCHANGE_COMPLETE) {
            throw new IllegalStateException(String.format("Unable to accept request payment for offer %s with state %s", id, state));
        }        
        apply(new OfferDebitRequestedEvent(this.id), metaData);
    }

    @EventHandler
    private void handleDebitRequested(OfferDebitRequestedEvent event) {
        this.state = OfferState.DEBIT_REQUESTED;
    }

    public void confirmDebit(MetaData metaData) {
        if (state != OfferState.DEBIT_REQUESTED) {
            throw new IllegalStateException(String.format("Unable to accept request payment for offer %s with state %s", id, state));
        }        
        apply(new OfferDebitConfirmedEvent(this.id), metaData);
    }

    @EventHandler
    private void handleCreditConfirmed(OfferDebitConfirmedEvent event) {
        this.state = OfferState.CLOSED;
    }
    
    public void cancel(MetaData metaData) {
        if (!Arrays.asList(OfferState.UNPAIRED).contains(state)) {
            throw new IllegalStateException(String.format("Unable to cancel offer %s with state %s", id, state));            
        }
        apply(new OfferCanceledEvent(id), metaData);
    }

    @EventHandler
    private void handleCanceled(OfferCanceledEvent event) {
        state = OfferState.CANCELED;
    }

    
}
