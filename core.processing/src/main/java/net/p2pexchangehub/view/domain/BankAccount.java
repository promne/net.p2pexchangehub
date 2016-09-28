package net.p2pexchangehub.view.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.annotation.Id;

import net.p2pexchangehub.core.domain.ExternalBankType;

public class BankAccount {

    @Id
    private String id;
    public static final String PROPERTY_ID = "id";

    private String currency;
    public static final String PROPERTY_CURRENCY = "currency";

    private BigDecimal balance;

    private Date lastCheck;

    private boolean active;
    public static final String PROPERTY_ACTIVE = "active";

    private boolean synchronizationEnabled;
    public static final String PROPERTY_SYNCHRONIZATION_ENABLED = "synchronizationEnabled";

    private String accountNumber;
    public static final String PROPERTY_ACCOUNT_NUMBER = "accountNumber";

    private ExternalBankType bankType;

    public BankAccount() {
        super();
    }

    public BankAccount(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Date getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public ExternalBankType getBankType() {
        return bankType;
    }

    public void setBankType(ExternalBankType bankType) {
        this.bankType = bankType;
    }

    public boolean isSynchronizationEnabled() {
        return synchronizationEnabled;
    }

    public void setSynchronizationEnabled(boolean synchronizationEnabled) {
        this.synchronizationEnabled = synchronizationEnabled;
    }

}
