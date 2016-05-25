package net.p2pexchangehub.core.api.user;

import java.util.UUID;

public class CreditOfferFromUserAccountCommand {

    // to track this transaction
    private final String transactionId = UUID.randomUUID().toString();

    private final String offerId;

    public CreditOfferFromUserAccountCommand(String offerId) {
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
