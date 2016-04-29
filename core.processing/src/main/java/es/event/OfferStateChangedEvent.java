package es.event;

import george.test.exchange.core.domain.offer.OfferState;

public class OfferStateChangedEvent {

    private final String offerId;
    
    private final OfferState newState;

    public OfferStateChangedEvent(String offerId, OfferState newState) {
        super();
        this.offerId = offerId;
        this.newState = newState;
    }

    public String getOfferId() {
        return offerId;
    }

    public OfferState getNewState() {
        return newState;
    }
    
}
