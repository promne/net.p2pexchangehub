package net.p2pexchangehub.core.handler.external.bank;

import java.util.Date;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class ExternalTransactionRequest {

    private final String transactionId;
    
    private final String userAccountId;
    
    private final String userBankAccountId;
    
    private final CurrencyAmount amount;

    private final Date timestamp;
    
    public ExternalTransactionRequest(String transactionId, String userAccountId, String userBankAccountId, CurrencyAmount amount, Date timestamp) {
        super();
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
        this.userBankAccountId = userBankAccountId;
        this.amount = amount;
        this.timestamp = timestamp;
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

    public Date getTimestamp() {
        return timestamp;
    }

    
}
