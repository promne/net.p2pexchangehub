package net.p2pexchangehub.core.util;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import george.test.exchange.core.domain.entity.CurrencyConfiguration;
import net.p2pexchangehub.view.repository.CurrencyConfigurationRepository;

@Singleton
@Startup
public class DemoData {
    
    public static final String BEAN_NAME = "DemoData";

    @Inject
    private CurrencyConfigurationRepository currencyConfigurationRepository;
    
    @PostConstruct
    public void init() {
        if (currencyConfigurationRepository.count()==0) {
            initSE();
        }
    }
    
    public void initSE() {
        currencyConfigurationRepository.save(new CurrencyConfiguration("CZK",1));
        currencyConfigurationRepository.save(new CurrencyConfiguration("NZD",2));
    }
    
    
}
