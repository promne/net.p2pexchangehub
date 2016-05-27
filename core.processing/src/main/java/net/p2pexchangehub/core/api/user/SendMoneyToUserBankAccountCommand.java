package net.p2pexchangehub.core.api.user;

import java.util.UUID;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class SendMoneyToUserBankAccountCommand {

    // to track this transaction
    private final String transactionId = UUID.randomUUID().toString();
    
    private final String userAccountId;
    
    private final String bankAccountId;
    
    private final CurrencyAmount amount;

    public SendMoneyToUserBankAccountCommand(String userAccountId, String bankAccountId, CurrencyAmount amount) {
        super();
        this.userAccountId = userAccountId;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
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
