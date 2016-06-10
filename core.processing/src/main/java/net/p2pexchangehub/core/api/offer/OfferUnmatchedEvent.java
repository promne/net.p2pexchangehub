package net.p2pexchangehub.core.api.offer;

public class OfferUnmatchedEvent {

    private final String offerId;

    private final String matchedOfferId;

    public OfferUnmatchedEvent(String offerId, String matchedOfferId) {
        super();
        this.offerId = offerId;
        this.matchedOfferId = matchedOfferId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getMatchedOfferId() {
        return matchedOfferId;
    }
    
}
