package net.p2pexchangehub.core.api.offer;

public class OfferExchangeCompletedEvent {

    private final String offerId;

    public OfferExchangeCompletedEvent(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
        
}
