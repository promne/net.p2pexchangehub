package es.event;

public class ConfigurationItemCreatedEvent {

    private final String id;

    public ConfigurationItemCreatedEvent(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }
        
}
