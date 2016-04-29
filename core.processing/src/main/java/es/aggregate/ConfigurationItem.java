package es.aggregate;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import es.event.ConfigurationItemCreatedEvent;
import es.event.ConfigurationItemValueChangedEvent;

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

    public ConfigurationItem(String id, String value) {
        super();
        apply(new ConfigurationItemCreatedEvent(id));
        apply(new ConfigurationItemValueChangedEvent(id, value));
    }

    @EventHandler
    private void handleCreated(ConfigurationItemCreatedEvent event) {
        this.id = event.getId();
    }
    
    public void setValue(String value) {
        apply(new ConfigurationItemValueChangedEvent(id, value));
    }
    
    @EventHandler
    private void handleValueChanged(ConfigurationItemValueChangedEvent event) {
        this.value = event.getValue();
    }
        
}
