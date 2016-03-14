package george.test.exchange.core.domain.entity.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import george.test.exchange.core.domain.ExternalBankType;

@Entity 
@Inheritance(strategy = InheritanceType.JOINED)
//@DiscriminatorColumn(name="EXTERNAL_BANK_ACCOUNT_TYPE", discriminatorType=DiscriminatorType.INTEGER)
public abstract class ExternalBankAccount {

    @Id
    private String id = UUID.randomUUID().toString();
    
    private String currency;
    
    private String country;
    
    private BigDecimal balance; 
    
    private Date lastCheck;
    
    private boolean active;
    
    private boolean deprecated;

    private String accountNumber;
    
    public abstract ExternalBankType getBankType();

    public abstract String getFullAccountNumber();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
        result = prime * result + ((currency == null) ? 0 : currency.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExternalBankAccount other = (ExternalBankAccount) obj;
        if (accountNumber == null) {
            if (other.accountNumber != null) {
                return false;
            }
        } else if (!accountNumber.equals(other.accountNumber)) {
            return false;
        }
        if (currency == null) {
            if (other.currency != null) {
                return false;
            }
        } else if (!currency.equals(other.currency)) {
            return false;
        }
        return true;
    }
    
}
