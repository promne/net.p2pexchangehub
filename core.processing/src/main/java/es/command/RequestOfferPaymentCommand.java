package es.command;

public class RequestOfferPaymentCommand {

    private final String offerId;

    public RequestOfferPaymentCommand(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
    
}
