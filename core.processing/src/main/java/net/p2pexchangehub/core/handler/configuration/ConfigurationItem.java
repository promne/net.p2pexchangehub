package net.p2pexchangehub.core.handler.configuration;

import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import net.p2pexchangehub.core.api.configuration.ConfigurationItemCreatedEvent;
import net.p2pexchangehub.core.api.configuration.ConfigurationItemValueChangedEvent;

public class ConfigurationItem extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;
    
    private String value;

    public ConfigurationItem() {
        super();
    }
    
    public String getValue() {
        return value;
    }
    
    public String getId() {
        return id;
    }

    public ConfigurationItem(String id, String value, MetaData metadata) {
        super();
        apply(new ConfigurationItemCreatedEvent(id), metadata);
        apply(new ConfigurationItemValueChangedEvent(id, value), metadata);
    }

    @EventHandler
    private void handleCreated(ConfigurationItemCreatedEvent event) {
        this.id = event.getId();
    }
    
    public void setValue(String value, MetaData metadata) {
        apply(new ConfigurationItemValueChangedEvent(id, value), metadata);
    }
    
    @EventHandler
    private void handleValueChanged(ConfigurationItemValueChangedEvent event) {
        this.value = event.getValue();
    }
        
}
