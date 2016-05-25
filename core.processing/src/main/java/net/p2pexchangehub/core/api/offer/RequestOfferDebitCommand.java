package net.p2pexchangehub.core.api.offer;

public class RequestOfferDebitCommand {

    private final String offerId;

    public RequestOfferDebitCommand(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
    
}
