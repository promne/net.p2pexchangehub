package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserIncomingTransactionMatchedEvent {

    private final String userAccountId;
    
    private final String transactionId;
    
    private final CurrencyAmount amount;
    
    private final CurrencyAmount newBalance;

    public UserIncomingTransactionMatchedEvent(String userAccountId, String transactionId, CurrencyAmount amount, CurrencyAmount newBalance) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public CurrencyAmount getNewBalance() {
        return newBalance;
    }

}
