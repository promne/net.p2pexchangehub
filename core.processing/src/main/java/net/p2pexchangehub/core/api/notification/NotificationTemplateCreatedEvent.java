package net.p2pexchangehub.core.api.notification;

public class NotificationTemplateCreatedEvent {

    private final String id;

    public NotificationTemplateCreatedEvent(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }
    
}
