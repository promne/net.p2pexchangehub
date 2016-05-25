package net.p2pexchangehub.core.api.offer;

import java.math.BigDecimal;

public class OfferMatchedEvent {
    
    private final String offerId;

    private final String matchedOfferId;

    private final BigDecimal amountOffered;

    private final BigDecimal amountRequested;

    public OfferMatchedEvent(String offerId, String matchedOfferId, BigDecimal amountOffered, BigDecimal amountRequested) {
        super();
        this.offerId = offerId;
        this.matchedOfferId = matchedOfferId;
        this.amountOffered = amountOffered;
        this.amountRequested = amountRequested;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getMatchedOfferId() {
        return matchedOfferId;
    }

    public BigDecimal getAmountOffered() {
        return amountOffered;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

}
