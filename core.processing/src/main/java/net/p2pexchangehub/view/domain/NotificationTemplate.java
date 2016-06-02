package net.p2pexchangehub.view.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class NotificationTemplate {
    
    @Id
    private String id;

    private Map<String, NotificationTemplateEmail> emailTemplates = new HashMap<>();
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, NotificationTemplateEmail> getEmailTemplates() {
        return emailTemplates;
    }

    public NotificationTemplateEmail getDefaultEmailTemplate() {
        Collection<NotificationTemplateEmail> values = emailTemplates.values();
        return values.isEmpty() ? null : values.iterator().next();
    }

    public void setEmailTemplates(Map<String, NotificationTemplateEmail> emailTemplates) {
        this.emailTemplates = emailTemplates;
    }
    
}

