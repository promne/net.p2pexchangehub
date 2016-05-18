package george.test.exchange.core.processing.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

@Stateless
public class NotificationService {

    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private Logger log;

    public NotificationService() {
        super();
    }
    
}
