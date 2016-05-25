package net.p2pexchangehub.core.api.offer;

public class OfferCreditDeclineReservedEvent {

    private final String offerId;

    private final String transactionId;

    private final String userAccountId;

    public OfferCreditDeclineReservedEvent(String offerId, String transactionId, String userAccountId) {
        super();
        this.offerId = offerId;
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getOfferId() {
        return offerId;
    }

}
