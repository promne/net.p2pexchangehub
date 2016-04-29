package es.event;

public class OfferOwnerBankNumberChangedEvent {

    private final String offerId;
    
    private final String newAccountNumber;

    public OfferOwnerBankNumberChangedEvent(String offerId, String newAccountNumber) {
        super();
        this.offerId = offerId;
        this.newAccountNumber = newAccountNumber;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getNewAccountNumber() {
        return newAccountNumber;
    }
        
}
