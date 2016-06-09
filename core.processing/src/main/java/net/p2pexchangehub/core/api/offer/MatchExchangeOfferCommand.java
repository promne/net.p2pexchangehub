package net.p2pexchangehub.core.api.offer;

import java.math.BigDecimal;

public class MatchExchangeOfferCommand {

    private final String newOfferId;

    private final String matchOfferId;
    
    private final String userAccountId;    
    
    private final BigDecimal amountOffered;

    private final BigDecimal amountRequested;

    public MatchExchangeOfferCommand(String newOfferId, String matchOfferId, String userAccountId, BigDecimal amountOffered, BigDecimal amountRequested) {
        super();
        this.newOfferId = newOfferId;
        this.matchOfferId = matchOfferId;
        this.userAccountId = userAccountId;
        this.amountOffered = amountOffered;
        this.amountRequested = amountRequested;
    }

    public String getNewOfferId() {
        return newOfferId;
    }

    public String getMatchOfferId() {
        return matchOfferId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public BigDecimal getAmountOffered() {
        return amountOffered;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    @Override
    public String toString() {
        return "MatchExchangeOfferCommand [newOfferId=" + newOfferId + ", matchOfferId=" + matchOfferId + ", userAccountId=" + userAccountId + ", amountOffered=" + amountOffered
                + ", amountRequested=" + amountRequested + "]";
    }
    
}
