package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserAccountDebitForOfferReservedEvent {

    private final String userAccountId;    
    
    private final String transactionId;    
    
    private final String offerId;
    
    private final CurrencyAmount amount;

    private final CurrencyAmount newBalance;

    public UserAccountDebitForOfferReservedEvent(String userAccountId, String transactionId, String offerId, CurrencyAmount amount, CurrencyAmount walletBalance) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
        this.offerId = offerId;
        this.amount = amount;
        this.newBalance = walletBalance;
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

    public CurrencyAmount getAmount() {
        return amount;
    }

    public CurrencyAmount getNewBalance() {
        return newBalance;
    }
    
}
