package net.p2pexchangehub.view.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;

import net.p2pexchangehub.core.handler.offer.OfferState;

public class Offer {

    @Id
    private String id;
    public static final String PROPERTY_ID = "id";

    private Date dateCreated;
    public static final String PROPERTY_DATE_CREATED = "dateCreated";    
    
    private String userAccountId;
    public static final String PROPERTY_USER_ACCOUNT_ID = "userAccountId";

    private String currencyOffered;
    public static final String PROPERTY_CURRENCY_OFFERED = "currencyOffered";

    private BigDecimal amountOfferedMin;
    public static final String PROPERTY_AMOUNT_OFFERED_MIN = "amountOfferedMin";

    private BigDecimal amountOfferedMax;
    public static final String PROPERTY_AMOUNT_OFFERED_MAX = "amountOfferedMax";

    private BigDecimal amountOffered;
    public static final String PROPERTY_AMOUNT_OFFERED = "amountOffered";

    private String currencyRequested;
    public static final String PROPERTY_CURRENCY_REQUESTED = "currencyRequested";

    private String requestedExchangeRateExpression;
    public static final String PROPERTY_REQUESTED_EXCHANGE_RATE_EXPRESSION = "requestedExchangeRateExpression";

    private BigDecimal amountRequested;
    public static final String PROPERTY_AMOUNT_REQUESTED = "amountRequested";

    private BigDecimal exchangeRate;
    public static final String PROPERTY_EXCHANGE_RATE = "exchangeRate";

    private OfferState state;
    public static final String PROPERTY_STATE = "state";

    private List<OfferStateHistory> stateHistory = new ArrayList<>();
    
    private String matchedExchangeOfferId;
    public static final String PROPERTY_MATCHED_EXCHANGE_OFFER_ID = "matchedExchangeOfferId";

    private String referenceId;
    public static final String PROPERTY_REFERENCE_ID = "referenceId";
    
    public Offer() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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

    public String getCurrencyRequested() {
        return currencyRequested;
    }

    public void setCurrencyRequested(String currencyRequested) {
        this.currencyRequested = currencyRequested;
    }

    public String getRequestedExchangeRateExpression() {
        return requestedExchangeRateExpression;
    }

    public void setRequestedExchangeRateExpression(String exchangeRateExpression) {
        this.requestedExchangeRateExpression = exchangeRateExpression;
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

    public void changeState(OfferState state, DateTime timestamp) {
        stateHistory.add(new OfferStateHistory(state, timestamp.toDate().toInstant()));
        this.state = state;
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

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    
}
