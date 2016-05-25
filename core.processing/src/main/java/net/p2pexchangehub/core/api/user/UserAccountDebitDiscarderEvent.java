package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserAccountDebitDiscarderEvent {

    private final String userAccountId;

    private final String transactionId;

    private final CurrencyAmount amount;

    public UserAccountDebitDiscarderEvent(String userAccountId, String transactionId, CurrencyAmount amount) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
        this.amount = amount;
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
    
}
