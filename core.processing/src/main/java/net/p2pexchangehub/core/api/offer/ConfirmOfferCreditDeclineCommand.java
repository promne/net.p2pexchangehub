package net.p2pexchangehub.core.api.offer;

public class ConfirmOfferCreditDeclineCommand {

    private final String offerId;
    
    private final String transactionId;

    public ConfirmOfferCreditDeclineCommand(String offerId, String transactionId) {
        super();
        this.offerId = offerId;
        this.transactionId = transactionId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getTransactionId() {
        return transactionId;
    }
    
}
