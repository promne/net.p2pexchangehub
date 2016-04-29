package es.command;

public class ChangeConfigurationItemValueCommand {

    private final String id;

    private final String value;

    public ChangeConfigurationItemValueCommand(String id, String value) {
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
