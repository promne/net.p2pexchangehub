package net.p2pexchangehub.core.handler.notification;

import javax.inject.Inject;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.MetaData;
import org.axonframework.repository.Repository;

import net.p2pexchangehub.core.api.notification.CreateNotificationTemplateCommand;
import net.p2pexchangehub.core.api.notification.UpdateEmailTemplateTextCommand;

public class NotificationTemplateCommandHandler {

    @Inject
    private Repository<NotificationTemplate> repository;
    
    @CommandHandler
    public void createNotificationTemplate(CreateNotificationTemplateCommand command, MetaData metadata) {
        NotificationTemplate template = new NotificationTemplate(command.getId(), metadata);
        repository.add(template);
    }

    @CommandHandler
    public void updateNotificationTemplate(UpdateEmailTemplateTextCommand command, MetaData metadata) {
        NotificationTemplate template = repository.load(command.getNotificationTemplateId());
        template.updateEmailTemplateText(command.getLanguageCode(), command.getSubject(), command.getText(), metadata);
    }
    
}
