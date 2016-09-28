package net.p2pexchangehub.core.domain.entity;

import java.math.BigDecimal;

import net.p2pexchangehub.core.domain.entity.bank.ExternalBankTransaction;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccount;

public class TransactionRequestExternal {

    private String id;
    
    private BigDecimal amount;
    
    private ExternalBankAccount bankAccount;

    private String recipientAccountNumber;
    
    private String detailInfo;
    
    private ExternalBankTransaction externalBankTransaction;    
    
    public TransactionRequestExternal() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public ExternalBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(ExternalBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public void setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    public ExternalBankTransaction getExternalBankTransaction() {
        return externalBankTransaction;
    }

    public void setExternalBankTransaction(ExternalBankTransaction externalBankTransaction) {
        this.externalBankTransaction = externalBankTransaction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    
}
