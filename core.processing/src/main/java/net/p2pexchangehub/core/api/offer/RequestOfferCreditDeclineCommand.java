package net.p2pexchangehub.core.api.offer;

import java.util.UUID;

public class RequestOfferCreditDeclineCommand {

    // to track this transaction
    private final String transactionId = UUID.randomUUID().toString();
    
    private final String offerId;        

    public RequestOfferCreditDeclineCommand(String offerId) {
        super();
        this.offerId = offerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOfferId() {
        return offerId;
    }
    
}
