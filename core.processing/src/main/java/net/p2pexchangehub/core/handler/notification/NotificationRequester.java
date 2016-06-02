package net.p2pexchangehub.core.handler.notification;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.user.RequestSendNotificationToUserAccountCommand;
import net.p2pexchangehub.core.api.user.contact.ContactDetailValidationRequestedEvent;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;

/**
 * Listens to different events in the system and requests proper notification to be sent. 
 *
 */
public class NotificationRequester extends AbstractIgnoreReplayEventHandler {

    public static final String TEMPLATE_SOURCE_EVENT = "TEMPLATE_SOURCE_EVENT";
    
    @Inject
    private CommandGateway gateway;

    @EventHandler
    public void handleRequest(ContactDetailValidationRequestedEvent event) {
        if (isLive()) {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put(TEMPLATE_SOURCE_EVENT, event);
            String notificationTemplateId = event.getClass().getCanonicalName();
            gateway.send(new RequestSendNotificationToUserAccountCommand(event.getUserAccountId(), notificationTemplateId , templateData ));
        }
    }
    
}
