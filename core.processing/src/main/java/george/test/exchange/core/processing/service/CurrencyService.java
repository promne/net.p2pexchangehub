package george.test.exchange.core.processing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.ejb.Stateless;
import javax.inject.Inject;

import net.p2pexchangehub.view.repository.CurrencyConfigurationRepository;

@Stateless
public class CurrencyService {

    @Inject
    private CurrencyConfigurationRepository repository;

    public BigDecimal calculateExchangePay(BigDecimal amount, String currencyOffered, String currencyRequested, BigDecimal exchangeRate) {
        int precision = repository.findOne(currencyRequested).getScale();
        BigDecimal result = amount.multiply(exchangeRate).setScale(precision, RoundingMode.HALF_UP);
        return result;
    }

}
