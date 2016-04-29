package es.command;

public class CreateConfigurationItemCommand {

    private final String id;

    private final String value;

    public CreateConfigurationItemCommand(String id, String value) {
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
