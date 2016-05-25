package net.p2pexchangehub.view.event;

import javax.inject.Inject;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import net.p2pexchangehub.core.api.configuration.ConfigurationItemCreatedEvent;
import net.p2pexchangehub.core.api.configuration.ConfigurationItemValueChangedEvent;
import net.p2pexchangehub.view.domain.ConfigurationItem;
import net.p2pexchangehub.view.repository.ConfigurationRepository;

public class ConfigurationItemListener implements ReplayAware {

    @Inject
    private ConfigurationRepository repository;
    
    @EventHandler
    public void itemCreated(ConfigurationItemCreatedEvent event) {
        repository.save(new ConfigurationItem(event.getId()));
    }

    @EventHandler
    public void itemChanged(ConfigurationItemValueChangedEvent event) {
        repository.updateValue(event.getId(), event.getValue());
//        ConfigurationItem configurationItem = repository.findOne(event.getId());
//        configurationItem.setValue(event.getValue());
//        em.merge(configurationItem);
    }

    @Override
    public void beforeReplay() {
        repository.deleteAll();
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
}
