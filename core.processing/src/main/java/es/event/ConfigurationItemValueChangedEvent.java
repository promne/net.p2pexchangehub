package es.event;

public class ConfigurationItemValueChangedEvent {

    private final String id;

    private final String value;

    public ConfigurationItemValueChangedEvent(String id, String value) {
        super();
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
    
}
