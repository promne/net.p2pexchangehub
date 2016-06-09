package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserAccountDebitForExternalBankAccountReservedEvent {

    private final String userAccountId;    
    
    private final String transactionId;    
    
    private final String bankAccountNumber;
    
    private final CurrencyAmount amount;

    private final CurrencyAmount newBalance;

    public UserAccountDebitForExternalBankAccountReservedEvent(String userAccountId, String transactionId, String bankAccountNumber, CurrencyAmount amount,
            CurrencyAmount newBalance) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
        this.bankAccountNumber = bankAccountNumber;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public CurrencyAmount getNewBalance() {
        return newBalance;
    }        
    
}
