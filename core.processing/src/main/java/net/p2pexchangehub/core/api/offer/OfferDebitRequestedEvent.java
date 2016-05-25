package net.p2pexchangehub.core.api.offer;

public class OfferDebitRequestedEvent {

    private final String offerId;

    public OfferDebitRequestedEvent(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }

}
