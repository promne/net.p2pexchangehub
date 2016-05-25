package net.p2pexchangehub.core.api.offer;

public class OfferDebitConfirmedEvent {

    private final String offerId;

    public OfferDebitConfirmedEvent(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
    
}
