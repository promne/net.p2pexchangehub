package george.test.exchange.core.processing.util;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import george.test.exchange.core.domain.entity.CurrencyConfiguration;

@Singleton
@Startup
public class DemoData {
    
    public static final String BEAN_NAME = "DemoData";

    @PersistenceContext
    private EntityManager em;
    
    @PostConstruct
    public void init() {
//        initSE();
    }
    
    public void initSE() {
        em.persist(new CurrencyConfiguration("CZK",1));
        em.persist(new CurrencyConfiguration("NZD",2));
    }
    
    
}
