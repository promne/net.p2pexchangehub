package net.p2pexchangehub.core.api.offer;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class OfferCreditedEvent {

    private final String offerId;
    
    private final String transactionId;

    private final String userAccountId;

    private final CurrencyAmount amount;

    public OfferCreditedEvent(String offerId, String transactionId, String userAccountId, CurrencyAmount amount) {
        super();
        this.offerId = offerId;
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public String getOfferId() {
        return offerId;
    }

}
