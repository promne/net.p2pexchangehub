package net.p2pexchangehub.core.api.offer;

public class CompleteOfferExchangeCommand {

    private final String offerId;

    public CompleteOfferExchangeCommand(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
        
}
