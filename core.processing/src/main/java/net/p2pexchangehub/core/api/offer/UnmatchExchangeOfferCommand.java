package net.p2pexchangehub.core.api.offer;

public class UnmatchExchangeOfferCommand {

    private final String offerId;

    public UnmatchExchangeOfferCommand(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
    
}
