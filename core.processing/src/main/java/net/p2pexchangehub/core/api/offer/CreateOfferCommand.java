package net.p2pexchangehub.core.api.offer;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateOfferCommand {

    private final String offerId;
    
    private final String userAccountId;
    
    private final String currencyOffered;
    
    private final BigDecimal amountOfferedMin;

    private final BigDecimal amountOfferedMax;
    
    private final String currencyRequested;

    private final String requestedExchangeRateExpression;

    public CreateOfferCommand(String offerId, String userAccountId, String currencyOffered, BigDecimal amountOfferedMin, BigDecimal amountOfferedMax, String currencyRequested,
            String requestedExchangeRateExpression) {
        super();
        this.offerId = offerId;
        this.userAccountId = userAccountId;
        this.currencyOffered = currencyOffered;
        this.amountOfferedMin = amountOfferedMin;
        this.amountOfferedMax = amountOfferedMax;
        this.currencyRequested = currencyRequested;
        this.requestedExchangeRateExpression = requestedExchangeRateExpression;
    }

    public CreateOfferCommand(String userAccountId, String currencyOffered, BigDecimal amountOfferedMin, BigDecimal amountOfferedMax, String currencyRequested,
            String requestedExchangeRateExpression) {
        this(UUID.randomUUID().toString(), userAccountId, currencyOffered, amountOfferedMin, amountOfferedMax, currencyRequested, requestedExchangeRateExpression);
    }

    public String getOfferId() {
        return offerId;
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

    public String getCurrencyRequested() {
        return currencyRequested;
    }

    public String getRequestedExchangeRateExpression() {
        return requestedExchangeRateExpression;
    }
    
}
