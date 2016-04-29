package george.test.exchange.core.processing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import george.test.exchange.core.domain.entity.CurrencyConfiguration;

@Stateless
public class CurrencyService {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private Logger log;

    public BigDecimal calculateExchangePay(BigDecimal amount, String currencyOffered, String currencyRequested, BigDecimal exchangeRate) {
        int precision = em.find(CurrencyConfiguration.class, currencyRequested).getScale();
        BigDecimal result = amount.multiply(exchangeRate).setScale(precision, RoundingMode.HALF_UP);
        return result;
    }

}
