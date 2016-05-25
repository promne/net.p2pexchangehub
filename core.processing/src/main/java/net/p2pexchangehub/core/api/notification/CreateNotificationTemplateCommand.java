package net.p2pexchangehub.core.api.notification;

public class CreateNotificationTemplateCommand {

    private final String id;

    public CreateNotificationTemplateCommand(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }
        
}
