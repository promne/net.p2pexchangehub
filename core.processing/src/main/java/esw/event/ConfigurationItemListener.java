package esw.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.eventhandling.annotation.EventHandler;

import es.event.ConfigurationItemCreatedEvent;
import es.event.ConfigurationItemValueChangedEvent;
import esw.domain.ConfigurationItem;

@Stateless
public class ConfigurationItemListener {

    @PersistenceContext
    private EntityManager em;

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
    
}
