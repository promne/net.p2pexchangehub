package net.p2pexchangehub.view.domain;

import java.time.Instant;

import net.p2pexchangehub.core.handler.offer.OfferState;

public class OfferStateHistory {

    private final OfferState state;

    private final Instant timestamp;
    
    public OfferStateHistory(OfferState state, Instant timestamp) {
        this.state = state;
        this.timestamp = timestamp;
    }

    public OfferState getState() {
        return state;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

}
