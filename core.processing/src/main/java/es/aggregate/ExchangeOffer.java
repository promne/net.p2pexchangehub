package es.aggregate;

import java.math.BigDecimal;
import java.util.Arrays;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import es.event.OfferCreatedEvent;
import es.event.OfferIncomingTransactionMatchedEvent;
import es.event.OfferMatchedEvent;
import es.event.OfferOwnerBankNumberChangedEvent;
import es.event.OfferStateChangedEvent;
import george.test.exchange.core.domain.offer.OfferState;

public class ExchangeOffer extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String userAccountId;

    private String currencyOffered;

    private BigDecimal amountOfferedMin;

    private BigDecimal amountOfferedMax;

    // how much was actually offered in the deal
    private BigDecimal amountOffered;

    // received amount towards amountOffered
    private BigDecimal amountReceived = BigDecimal.ZERO;

    private String currencyRequested;

    private BigDecimal amountRequestedExchangeRate;

    // how much will the user actually get
    private BigDecimal amountRequested;

    private OfferState state;

    private String ownerAccountNumber;

    private String matchedExchangeOfferId;

    private String referenceId;

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

    public BigDecimal getAmountReceived() {
        return amountReceived;
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

    public String getOwnerAccountNumber() {
        return ownerAccountNumber;
    }

    public String getMatchedExchangeOfferId() {
        return matchedExchangeOfferId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    @EventHandler
    private void handleStateChanged(OfferStateChangedEvent event) {
        this.state = event.getNewState();
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
        apply(new OfferStateChangedEvent(offerId, OfferState.UNPAIRED));
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
    }
    
    public void matchWithOffer(String matchedOfferId, BigDecimal amountOffered, BigDecimal amountRequested, String referenceId) {
        if (this.state != OfferState.UNPAIRED) {
            throw new IllegalStateException(String.format("Unable to match offer %s in state %s", this.id, this.state));
        }
        if (amountOffered.compareTo(this.amountOfferedMin) < 0 || amountOffered.compareTo(this.amountOfferedMax) > 0) {
            throw new IllegalStateException(String.format("Offered amount %s is out of the offer %s - %s range", amountOffered, this.amountOfferedMin, this.amountOfferedMax));
        }
        if (amountOffered.multiply(this.amountRequestedExchangeRate).compareTo(amountRequested)>0) {
            throw new IllegalStateException(String.format("Reqeusted exchange of %s for %s doesn't match exchange rate %s of offer %s", amountOffered, amountRequested, this.amountRequestedExchangeRate, this.id));            
        }
        
        apply(new OfferMatchedEvent(this.id, matchedOfferId, amountOffered, amountRequested, referenceId));
        apply(new OfferStateChangedEvent(this.id, OfferState.WAITING_FOR_PAYMENT));
    }
    
    @EventHandler
    private void handleMatched(OfferMatchedEvent event) {
        this.amountOffered = event.getAmountOffered();
        this.amountRequested = event.getAmountRequested();
        this.matchedExchangeOfferId = event.getMatchedOfferId();
    }

    public void matchIncomingTransaction(String transactionId, BigDecimal amount) {
        if (state != OfferState.WAITING_FOR_PAYMENT) {
            throw new IllegalStateException(String.format("Unable to accept incoming transaction %s for offer %s with state %s", transactionId, id, state));
        }
        BigDecimal newBalance = amountReceived.add(amount);
        if (newBalance.compareTo(amountOffered) > 0) {
            throw new IllegalStateException(String.format("Unable to accept additional %s for offer %s from transaction %s", amount, id, transactionId));
        }
        apply(new OfferIncomingTransactionMatchedEvent(id, transactionId, amount, newBalance));
        if (newBalance.compareTo(amountOffered) == 0) {
            apply(new OfferStateChangedEvent(this.id, OfferState.PAYMENT_RECEIVED));
        }        
    }

    @EventHandler
    private void handleIncomingTransactionMatched(OfferIncomingTransactionMatchedEvent event) {
        this.amountReceived = event.getNewBalance();
    }

    public void requestPayment() {
        if (state != OfferState.PAYMENT_RECEIVED) {
            throw new IllegalStateException(String.format("Unable to accept request payment for offer %s with state %s", id, state));
        }        
        apply(new OfferStateChangedEvent(this.id, OfferState.SEND_MONEY_REQUESTED));
    }

    public void setOwnerAccountNumber(String accountNumber) {
        if (!Arrays.asList(OfferState.UNPAIRED, OfferState.PAYMENT_RECEIVED, OfferState.WAITING_FOR_PAYMENT).contains(state)) {
            throw new IllegalStateException(String.format("Unable to set owner account number for offer %s with state %s", id, state));            
        }
        apply(new OfferOwnerBankNumberChangedEvent(id, accountNumber));
    }

    @EventHandler
    private void handleOfferOwnerBankNumberChanged(OfferOwnerBankNumberChangedEvent event) {
        ownerAccountNumber = event.getNewAccountNumber();
    }
    
}
