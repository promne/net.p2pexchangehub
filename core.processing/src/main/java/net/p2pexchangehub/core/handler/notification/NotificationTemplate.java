package net.p2pexchangehub.core.handler.notification;

import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import net.p2pexchangehub.core.api.notification.EmailTemplateTextUpdatedEvent;
import net.p2pexchangehub.core.api.notification.NotificationTemplateCreatedEvent;

public class NotificationTemplate extends AbstractAnnotatedAggregateRoot<String> {

    public enum NotificationType {
        EMAIL,
        SMS
    }
    
    @AggregateIdentifier
    private String id;

    public NotificationTemplate() {
        super();
    }

    public NotificationTemplate(String id, MetaData metadata) {
        super();
        apply(new NotificationTemplateCreatedEvent(id), metadata);
    }

    @EventHandler
    private void handleCreated(NotificationTemplateCreatedEvent event) {
        this.id = event.getId();
    }
    
    public void updateEmailTemplateText(String languageCode, String subject, String text, MetaData metadata) {
        apply(new EmailTemplateTextUpdatedEvent(id, languageCode, subject, text), metadata);
    }
    
    @EventHandler
    private void handleEmailTemplateUpdated(EmailTemplateTextUpdatedEvent event) {
        //do nothing
    }
        
}
