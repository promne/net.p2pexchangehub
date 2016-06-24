package net.p2pexchangehub.client.web.dialog.sendmoney;

import java.math.BigDecimal;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.view.domain.UserBankAccount;

public class TransferRequest {
    
    private String currency;
    
    private BigDecimal amount;
    
    private UserBankAccount bankAccount;

    public TransferRequest(CurrencyAmount currencyAmount) {
        if (currencyAmount!=null) {
            this.currency = currencyAmount.getCurrencyCode();
            this.amount = currencyAmount.getAmount();
        }
    }
    
    public TransferRequest(String currency, BigDecimal amount) {
        super();
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public UserBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(UserBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }
            
}