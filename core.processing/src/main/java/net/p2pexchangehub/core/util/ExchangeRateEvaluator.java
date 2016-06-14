package net.p2pexchangehub.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;

public class ExchangeRateEvaluator {

    public static final int RATE_PRECISION = 4;
    
    public BigDecimal calculateExchangePay(BigDecimal amount, String currencyOffered, String currencyRequested, String exchangeRateExpression) {
        Currency currency = Currency.getInstance(currencyRequested);
        return amount.multiply(evaluate(exchangeRateExpression)).setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }

    public BigDecimal evaluate(String rateExpression) {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
        ELContext context = new StandardELContext(expressionFactory);
        ValueExpression valueExpression = expressionFactory.createValueExpression(context, rateExpression, BigDecimal.class);
        
        Object value = valueExpression.getValue(context);
        BigDecimal bigResult = (BigDecimal) value;
        return bigResult.setScale(RATE_PRECISION, RoundingMode.HALF_UP);        
    }

    public BigDecimal calculateRateRounded(BigDecimal offered, BigDecimal requested) {
        return requested.divide(offered, ExchangeRateEvaluator.RATE_PRECISION, RoundingMode.DOWN);        
    }
    
}
