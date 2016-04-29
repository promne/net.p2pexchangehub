package esw.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import george.test.exchange.core.domain.offer.OfferState;

@Entity
public class Offer {

    @Id
    private String id;

    private String userAccountId;

    private String currencyOffered;

    @Column(precision=10, scale=4)
    private BigDecimal amountOfferedMin;

    @Column(precision=10, scale=4)
    private BigDecimal amountOfferedMax;

    @Column(precision=10, scale=4)
    private BigDecimal amountOffered;

    @Column(precision=10, scale=4)
    private BigDecimal amountReceived = BigDecimal.ZERO;

    private String currencyRequested;

    @Column(precision=10, scale=4)
    private BigDecimal amountRequestedExchangeRate;

    @Column(precision=10, scale=4)
    private BigDecimal amountRequested;

    private OfferState state;
    public static final String STATE_PROPERTY = "state";

    private String ownerAccountNumber;

    private String matchedExchangeOfferId;

    private String referenceId;

    private String incomimgPaymentBankAccountId;

    public Offer() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(String userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getCurrencyOffered() {
        return currencyOffered;
    }

    public void setCurrencyOffered(String currencyOffered) {
        this.currencyOffered = currencyOffered;
    }

    public BigDecimal getAmountOfferedMin() {
        return amountOfferedMin;
    }

    public void setAmountOfferedMin(BigDecimal amountOfferedMin) {
        this.amountOfferedMin = amountOfferedMin;
    }

    public BigDecimal getAmountOfferedMax() {
        return amountOfferedMax;
    }

    public void setAmountOfferedMax(BigDecimal amountOfferedMax) {
        this.amountOfferedMax = amountOfferedMax;
    }

    public BigDecimal getAmountOffered() {
        return amountOffered;
    }

    public void setAmountOffered(BigDecimal amountOffered) {
        this.amountOffered = amountOffered;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getCurrencyRequested() {
        return currencyRequested;
    }

    public void setCurrencyRequested(String currencyRequested) {
        this.currencyRequested = currencyRequested;
    }

    public BigDecimal getAmountRequestedExchangeRate() {
        return amountRequestedExchangeRate;
    }

    public void setAmountRequestedExchangeRate(BigDecimal amountRequestedExchangeRate) {
        this.amountRequestedExchangeRate = amountRequestedExchangeRate;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public OfferState getState() {
        return state;
    }

    public void setState(OfferState state) {
        this.state = state;
    }

    public String getOwnerAccountNumber() {
        return ownerAccountNumber;
    }

    public void setOwnerAccountNumber(String ownerAccountNumber) {
        this.ownerAccountNumber = ownerAccountNumber;
    }

    public String getMatchedExchangeOfferId() {
        return matchedExchangeOfferId;
    }

    public void setMatchedExchangeOfferId(String matchedExchangeOfferId) {
        this.matchedExchangeOfferId = matchedExchangeOfferId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getIncomimgPaymentBankAccountId() {
        return incomimgPaymentBankAccountId;
    }

    public void setIncomimgPaymentBankAccountId(String incomimgPaymentBankAccountId) {
        this.incomimgPaymentBankAccountId = incomimgPaymentBankAccountId;
    }
    
}
