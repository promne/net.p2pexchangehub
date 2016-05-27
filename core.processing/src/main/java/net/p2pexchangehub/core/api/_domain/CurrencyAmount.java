package net.p2pexchangehub.core.api._domain;

import java.math.BigDecimal;

public class CurrencyAmount {

    private final String currencyCode;
    
    private final BigDecimal amount;

    public CurrencyAmount(String currencyCode, BigDecimal amount) {
        super();
        this.currencyCode = currencyCode;
        this.amount = amount;
    }
    
    public CurrencyAmount add(CurrencyAmount currencyAmount) {
        if (!this.currencyCode.equals(currencyAmount.currencyCode)) {
            throw new IllegalArgumentException("This instance currency "+currencyCode + " doesn't match " + currencyAmount.currencyCode);
        }
        return new CurrencyAmount(currencyCode, amount.add(currencyAmount.amount));
    }

    public CurrencyAmount subtract(CurrencyAmount currencyAmount) {
        if (!this.currencyCode.equals(currencyAmount.currencyCode)) {
            throw new IllegalArgumentException("This instance currency "+currencyCode + " doesn't match " + currencyAmount.currencyCode);
        }
        return new CurrencyAmount(currencyCode, amount.subtract(currencyAmount.amount));
    }
    
    public boolean isNotNegative() {
        return BigDecimal.ZERO.compareTo(amount)<=0;
    }
    
    public CurrencyAmount negate() {
        return new CurrencyAmount(currencyCode, amount.negate());
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : ((Double)amount.doubleValue()).hashCode());
        result = prime * result + ((currencyCode == null) ? 0 : currencyCode.hashCode());
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
        CurrencyAmount other = (CurrencyAmount) obj;
        if (amount == null) {
            if (other.amount != null)
                return false;
        } else if (amount.compareTo(other.amount)!=0)
            return false;
        if (currencyCode == null) {
            if (other.currencyCode != null)
                return false;
        } else if (!currencyCode.equals(other.currencyCode))
            return false;
        return true;
    }

}
