package net.p2pexchangehub.view.event;

import javax.inject.Inject;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import net.p2pexchangehub.core.api.notification.EmailTemplateTextUpdatedEvent;
import net.p2pexchangehub.core.api.notification.NotificationTemplateCreatedEvent;
import net.p2pexchangehub.view.domain.NotificationTemplate;
import net.p2pexchangehub.view.domain.NotificationTemplateEmail;
import net.p2pexchangehub.view.repository.NotificationTemplateRepository;

public class NotificationTemplateListener implements ReplayAware {

    @Inject
    private NotificationTemplateRepository repository;
    
    @EventHandler
    public void handleCreated(NotificationTemplateCreatedEvent event) {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(event.getId());
        repository.save(template);
    }

    @EventHandler
    public void handleEmailTemplateUpdated(EmailTemplateTextUpdatedEvent event) {
        NotificationTemplate template = repository.findOne(event.getNotificationTemplateId());
        
        NotificationTemplateEmail emailTemplate = new NotificationTemplateEmail();
        emailTemplate.setTemplateId(event.getNotificationTemplateId());
        emailTemplate.setLanguageCode(event.getLanguageCode());
        emailTemplate.setSubject(event.getSubject());
        emailTemplate.setText(event.getText());
        
        template.getEmailTemplates().put(event.getLanguageCode(), emailTemplate);
        
        repository.save(template);
    }
    
    @Override
    public void beforeReplay() {
        repository.deleteAll();
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
        
}
