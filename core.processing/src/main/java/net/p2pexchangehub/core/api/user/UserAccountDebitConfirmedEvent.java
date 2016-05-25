package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserAccountDebitConfirmedEvent {

    private final String userAccountId;

    private final String transactionId;

    private final CurrencyAmount amount;

    public UserAccountDebitConfirmedEvent(String userAccountId, String transactionId, CurrencyAmount amount) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
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

}
