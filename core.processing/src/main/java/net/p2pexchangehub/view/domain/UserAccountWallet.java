package net.p2pexchangehub.view.domain;

import java.math.BigDecimal;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserAccountWallet {

    private String currency;
    public static final String PROPERTY_CURRENCY = "currency";
    
    private BigDecimal amount;

    public UserAccountWallet(String currency, BigDecimal amount) {
        super();
        this.currency = currency;
        this.amount = amount;
    }

    public UserAccountWallet() {
        super();
    }

    public UserAccountWallet(CurrencyAmount newBalance) {
        this(newBalance.getCurrencyCode(), newBalance.getAmount());
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
    public String toString() {
        return currency + ":" + amount;
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
