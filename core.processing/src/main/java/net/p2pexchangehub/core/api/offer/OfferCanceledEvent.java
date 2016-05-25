package net.p2pexchangehub.core.api.offer;

public class OfferCanceledEvent {

    private final String offerId;

    public OfferCanceledEvent(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }

}
