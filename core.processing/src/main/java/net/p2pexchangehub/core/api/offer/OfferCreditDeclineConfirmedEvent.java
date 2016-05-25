package net.p2pexchangehub.core.api.offer;

public class OfferCreditDeclineConfirmedEvent {

    private final String offerId;

    private final String transactionId;

    private final String userAccountId;

    public OfferCreditDeclineConfirmedEvent(String offerId, String transactionId, String userAccountId) {
        super();
        this.offerId = offerId;
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

}
