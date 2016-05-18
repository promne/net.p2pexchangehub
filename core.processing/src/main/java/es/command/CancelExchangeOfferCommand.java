package es.command;

public class CancelExchangeOfferCommand {

    private final String offerId;

    public CancelExchangeOfferCommand(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
        
}
