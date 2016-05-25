package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class SendMoneyToUserBankAccountCommand {

    private final String userAccountId;
    
    private final String bankAccountId;
    
    private final CurrencyAmount amount;

    public SendMoneyToUserBankAccountCommand(String userAccountId, String bankAccountId, CurrencyAmount amount) {
        super();
        this.userAccountId = userAccountId;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
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
        
}
