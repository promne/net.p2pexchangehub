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
    public static final String PROPERTY_ID = "id";

    private String userAccountId;
    public static final String PROPERTY_USER_ACCOUNT_ID = "userAccountId";

    private String currencyOffered;
    public static final String PROPERTY_CURRENCY_OFFERED = "currencyOffered";

    @Column(precision=10, scale=4)
    private BigDecimal amountOfferedMin;
    public static final String PROPERTY_AMOUNT_OFFERED_MIN = "amountOfferedMin";

    @Column(precision=10, scale=4)
    private BigDecimal amountOfferedMax;
    public static final String PROPERTY_AMOUNT_OFFERED_MAX = "amountOfferedMax";

    @Column(precision=10, scale=4)
    private BigDecimal amountOffered;
    public static final String PROPERTY_AMOUNT_OFFERED = "amountOffered";

    @Column(precision=10, scale=4)
    private BigDecimal amountReceived = BigDecimal.ZERO;
    public static final String PROPERTY_AMOUNT_RECEIVED = "amountReceived";

    @Column(precision=10, scale=4)
    private BigDecimal amountSent = BigDecimal.ZERO;
    public static final String PROPERTY_AMOUNT_SENT = "amountSent";

    private String currencyRequested;
    public static final String PROPERTY_CURRENCY_REQUESTED = "currencyRequested";

    @Column(precision=10, scale=4)
    private BigDecimal amountRequestedExchangeRate;
    public static final String PROPERTY_AMOUNT_REQUESTED_EXCHANGE_RATE = "amountRequestedExchangeRate";

    @Column(precision=10, scale=4)
    private BigDecimal amountRequested;
    public static final String PROPERTY_AMOUNT_REQUESTED = "amountRequestedExchangeRate";

    private OfferState state;
    public static final String PROPERTY_STATE = "state";

    private String ownerAccountNumber;
    public static final String PROPERTY_OWNER_ACCOUNT_NUMBER = "ownerAccountNumber";

    private String matchedExchangeOfferId;
    public static final String PROPERTY_MATCHED_EXCHANGE_OFFER_ID = "matchedExchangeOfferId";

    private String referenceId;
    public static final String PROPERTY_REFERENCE_ID = "referenceId";

    private String incomingPaymentBankAccountId;
    public static final String PROPERTY_INCOMING_PAYMENT_BANK_ACCOUNT_ID = "incomingPaymentBankAccountId";

    private String outgoingPaymentBankAccountId;
    
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

    public String getIncomingPaymentBankAccountId() {
        return incomingPaymentBankAccountId;
    }

    public void setIncomingPaymentBankAccountId(String incomingPaymentBankAccountId) {
        this.incomingPaymentBankAccountId = incomingPaymentBankAccountId;
    }

    public String getOutgoingPaymentBankAccountId() {
        return outgoingPaymentBankAccountId;
    }

    public void setOutgoingPaymentBankAccountId(String outgoingPaymentBankAccountId) {
        this.outgoingPaymentBankAccountId = outgoingPaymentBankAccountId;
    }

    public BigDecimal getAmountSent() {
        return amountSent;
    }

    public void setAmountSent(BigDecimal amountSent) {
        this.amountSent = amountSent;
    }
    
}
