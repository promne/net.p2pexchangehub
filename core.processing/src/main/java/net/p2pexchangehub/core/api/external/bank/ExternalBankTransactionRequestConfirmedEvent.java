package net.p2pexchangehub.core.api.external.bank;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class ExternalBankTransactionRequestConfirmedEvent {

    private final String bankAccountId;

    private final String transactionId;
    
    private final String userAccountId;
    
    private final String userBankAccountId;
    
    private final CurrencyAmount amount;

    private final String externalBankTransactionId;

    public ExternalBankTransactionRequestConfirmedEvent(String bankAccountId, String transactionId, String userAccountId, String userBankAccountId, CurrencyAmount amount, String externalBankTransactionId) {
        super();
        this.bankAccountId = bankAccountId;
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
        this.userBankAccountId = userBankAccountId;
        this.amount = amount;
        this.externalBankTransactionId = externalBankTransactionId;
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

    public String getUserBankAccountId() {
        return userBankAccountId;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public String getExternalBankTransactionId() {
        return externalBankTransactionId;
    }
    
}
