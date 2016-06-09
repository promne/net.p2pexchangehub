package net.p2pexchangehub.core.api.offer;

import java.math.BigDecimal;

public class OfferRequestedAmountChangedEvent {

    private final String offerId;
    
    private final BigDecimal amountOfferedMin;

    private final BigDecimal amountOfferedMax;

    private final String exchangeRateExpression;

    public OfferRequestedAmountChangedEvent(String offerId, BigDecimal amountOfferedMin, BigDecimal amountOfferedMax, String exchangeRateExpression) {
        super();
        this.offerId = offerId;
        this.amountOfferedMin = amountOfferedMin;
        this.amountOfferedMax = amountOfferedMax;
        this.exchangeRateExpression = exchangeRateExpression;
    }

    public String getOfferId() {
        return offerId;
    }

    public BigDecimal getAmountOfferedMin() {
        return amountOfferedMin;
    }

    public BigDecimal getAmountOfferedMax() {
        return amountOfferedMax;
    }

    public String getExchangeRateExpression() {
        return exchangeRateExpression;
    }
    
}
