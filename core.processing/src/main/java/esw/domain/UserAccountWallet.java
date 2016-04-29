package esw.domain;

import java.math.BigDecimal;

import javax.persistence.Embeddable;

@Embeddable
public class UserAccountWallet {

    private String currency;
    
    private BigDecimal amount;

    public UserAccountWallet(String currency, BigDecimal amount) {
        super();
        this.currency = currency;
        this.amount = amount;
    }

    public UserAccountWallet() {
        super();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currency == null) ? 0 : currency.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserAccountWallet other = (UserAccountWallet) obj;
        if (currency == null) {
            if (other.currency != null)
                return false;
        } else if (!currency.equals(other.currency))
            return false;
        return true;
    }
        
}
