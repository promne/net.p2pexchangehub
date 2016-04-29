package esw.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import george.test.exchange.core.domain.ExternalBankType;

@Entity
public class BankAccount {

    @Id
    private String id;

    private String currency;

    private String country;

    private BigDecimal balance;

    private Date lastCheck;

    private boolean active;

    private String accountNumber;

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

}
