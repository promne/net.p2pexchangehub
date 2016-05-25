package net.p2pexchangehub.core.handler.notification;

import javax.inject.Inject;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;

import net.p2pexchangehub.core.api.notification.CreateNotificationTemplateCommand;

public class NotificationTemplateCommandHandler {

    @Inject
    private Repository<NotificationTemplate> repository;
    
    @CommandHandler
    public void createNotificationTemplate(CreateNotificationTemplateCommand command) {
        NotificationTemplate template = new NotificationTemplate(command.getId());
        repository.add(template);
    }
    
}
