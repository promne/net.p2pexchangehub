package net.p2pexchangehub.core.api.offer;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class CreditOfferCommand {

    private final String userAccountId;
    
    private final String transactionId;

    private final String offerId;

    private final CurrencyAmount amount;

    public CreditOfferCommand(String userAccountId, String transactionId, String offerId, CurrencyAmount amount) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
        this.offerId = offerId;
        this.amount = amount;
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

    public CurrencyAmount getAmount() {
        return amount;
    }

}
