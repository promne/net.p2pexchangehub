package net.p2pexchangehub.core.api.external.bank;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class RequestExternalBankTransactionCommand {

    private final String bankAccountId;
    
    private final String transactionId;    

    private final String userAccountId;    
    
    private final String bankAccountNumber;
    
    private final CurrencyAmount amount;

    public RequestExternalBankTransactionCommand(String bankAccountId, String transactionId, String userAccountId, String bankAccountNumber, CurrencyAmount amount) {
        super();
        this.bankAccountId = bankAccountId;
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
        this.bankAccountNumber = bankAccountNumber;
        this.amount = amount;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }
    
}
