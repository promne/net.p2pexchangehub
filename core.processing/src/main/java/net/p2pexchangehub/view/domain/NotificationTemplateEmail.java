package net.p2pexchangehub.view.domain;

public class NotificationTemplateEmail {

    private String templateId;
    
    private String languageCode;
    public static final String PROPERTY_LANGUAGE_CODE = "languageCode";
    
    private String subject;
    
    private String text;

    public NotificationTemplateEmail() {
        super();
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    
}
