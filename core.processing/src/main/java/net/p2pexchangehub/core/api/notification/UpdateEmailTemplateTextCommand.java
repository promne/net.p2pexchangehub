package net.p2pexchangehub.core.api.notification;

public class UpdateEmailTemplateTextCommand {

    private String notificationTemplateId;

    private final String languageCode;
    
    private final String subject;
    
    private final String text;

    public UpdateEmailTemplateTextCommand(String notificationTemplateId, String languageCode, String subject, String text) {
        super();
        this.notificationTemplateId = notificationTemplateId;
        this.languageCode = languageCode;
        this.subject = subject;
        this.text = text;
    }

    public String getNotificationTemplateId() {
        return notificationTemplateId;
    }

    public void setNotificationTemplateId(String notificationTemplateId) {
        this.notificationTemplateId = notificationTemplateId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }
    
}
