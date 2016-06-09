package net.p2pexchangehub.core.api.offer;

public class OfferCreatedEvent {

    private final String offerId;
    
    private final String userAccountId;

    private final String currencyOffered;

    private final String currencyRequested;

    public OfferCreatedEvent(String offerId, String userAccountId, String currencyOffered, String currencyRequested) {
        super();
        this.offerId = offerId;
        this.userAccountId = userAccountId;
        this.currencyOffered = currencyOffered;
        this.currencyRequested = currencyRequested;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getCurrencyOffered() {
        return currencyOffered;
    }

    public String getCurrencyRequested() {
        return currencyRequested;
    }

    public String getOfferId() {
        return offerId;
    }

}
