package esw.event;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import es.event.ConfigurationItemCreatedEvent;
import es.event.ConfigurationItemValueChangedEvent;
import esw.domain.ConfigurationItem;
import george.test.exchange.core.processing.util.JPAUtilsBean;

@Transactional
public class ConfigurationItemListener implements ReplayAware {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private JPAUtilsBean jpaUtils;
    
    @EventHandler
    public void itemCreated(ConfigurationItemCreatedEvent event) {
        em.persist(new ConfigurationItem(event.getId()));
    }

    @EventHandler
    public void itemChanged(ConfigurationItemValueChangedEvent event) {
        ConfigurationItem configurationItem = em.find(ConfigurationItem.class, event.getId());
        configurationItem.setValue(event.getValue());
        em.merge(configurationItem);
    }

    @Override
    public void beforeReplay() {
        jpaUtils.deleteAll(ConfigurationItem.class);
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
}
