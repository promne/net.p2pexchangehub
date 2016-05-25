package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class MoneyForCashoutReservedEvent {

    private final String userAccountId;
    
    private final String bankAccountId;
    
    private final CurrencyAmount amount;

    private final CurrencyAmount newBalance;

    public MoneyForCashoutReservedEvent(String userAccountId, String bankAccountId, CurrencyAmount amount, CurrencyAmount newBalance) {
        super();
        this.userAccountId = userAccountId;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public CurrencyAmount getNewBalance() {
        return newBalance;
    }
        
}
