package es.event;

public class OfferIncomingPaymentExternalAccountChangedEvent {

    private final String offerId;
    
    private final String bankAccountId;

    public OfferIncomingPaymentExternalAccountChangedEvent(String offerId, String bankAccountId) {
        super();
        this.offerId = offerId;
        this.bankAccountId = bankAccountId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

}
