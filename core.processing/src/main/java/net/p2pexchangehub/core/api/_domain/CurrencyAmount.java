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
    
    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    
}
