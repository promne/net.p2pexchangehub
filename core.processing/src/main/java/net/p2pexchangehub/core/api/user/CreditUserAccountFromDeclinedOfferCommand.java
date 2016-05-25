package net.p2pexchangehub.core.api.user;

public class CreditUserAccountFromDeclinedOfferCommand {

    private final String userAccountId;

    private final String transactionId;

    private final String offerId;

    public CreditUserAccountFromDeclinedOfferCommand(String userAccountId, String transactionId, String offerId) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
        this.offerId = offerId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOfferId() {
        return offerId;
    }
    
}
