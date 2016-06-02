package net.p2pexchangehub.core.api.user;

import java.util.Map;

public class UserAccountNotificationSendRequestedEvent {

    private final String userAccountId;

    private final String notificationTemplateId;
    
    private final Map<String, Object> templateData;

    public UserAccountNotificationSendRequestedEvent(String userAccountId, String notificationTemplateId, Map<String, Object> templateData) {
        super();
        this.userAccountId = userAccountId;
        this.notificationTemplateId = notificationTemplateId;
        this.templateData = templateData;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getNotificationTemplateId() {
        return notificationTemplateId;
    }

    public Map<String, Object> getTemplateData() {
        return templateData;
    }
    
}
