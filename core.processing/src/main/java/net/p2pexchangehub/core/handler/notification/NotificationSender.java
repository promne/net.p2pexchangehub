package net.p2pexchangehub.core.handler.notification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.MessagingException;

import org.apache.commons.io.output.StringBuilderWriter;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.slf4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.p2pexchangehub.core.api.user.UserAccountNotificationSendRequestedEvent;
import net.p2pexchangehub.core.api.user.contact.ContactDetailValidationRequestedEvent;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;
import net.p2pexchangehub.view.domain.NotificationTemplate;
import net.p2pexchangehub.view.domain.NotificationTemplateEmail;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact;
import net.p2pexchangehub.view.repository.ConfigurationRepository;
import net.p2pexchangehub.view.repository.NotificationTemplateRepository;
import net.p2pexchangehub.view.repository.UserAccountRepository;

/**
 * Sends actual notification 
 *
 */
public class NotificationSender extends AbstractIgnoreReplayEventHandler {

    public static final String CONFIG_EMAIL_SENDER_DEFAULT = "email.sender.default.address";
    
    public static final String CONFIG_SMTP_SERVER_HOST = "smtp.server.host";
    public static final String CONFIG_SMTP_SERVER_PORT = "smtp.server.port";
    
    private Configuration freemarkerConfig;

    @Inject
    private Logger logger;
    
    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;
    
    @Inject
    private NotificationTemplateRepository notificationTemplateRepository;

    @Inject
    private ConfigurationRepository configurationRepository;
    
    @PostConstruct
    public void init() {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_23);
    }
    
    @EventHandler
    public void handleRequest(UserAccountNotificationSendRequestedEvent event) throws TemplateException, IOException {
        if (isLive()) {
            //skip a lot of NPE situations here. Because lazy
            
            NotificationTemplate notificationTemplate = notificationTemplateRepository.findOne(event.getNotificationTemplateId());
            if (notificationTemplate != null) {
                UserAccount findOne = userAccountRepository.findOne(event.getUserAccountId());
                Map<String, Object> templateData = new HashMap<>(event.getTemplateData());
                
                templateData.put(UserAccount.class.getSimpleName(), findOne);
                
                if (event.getNotificationTemplateId().equals(ContactDetailValidationRequestedEvent.class.getCanonicalName())) {
                    ContactDetailValidationRequestedEvent srcEvent = (ContactDetailValidationRequestedEvent) event.getTemplateData().get(NotificationRequester.TEMPLATE_SOURCE_EVENT);
                    UserAccountContact contact = findOne.getContact(srcEvent.getContactId()).get();
                    
                    templateData.put(UserAccountContact.class.getSimpleName(), contact);
                    
                    switch (contact.getType()) {
                        case EMAIL:
                            sendEmail(findOne, contact, notificationTemplate.getDefaultEmailTemplate(), templateData);
                            break;
                    }
                    
                }
            }
        }
    }

    private void sendEmail(UserAccount findOne, UserAccountContact contact, NotificationTemplateEmail emailTemplate, Map<String, Object> templateData) throws TemplateException, IOException {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(configurationRepository.getValueString(CONFIG_SMTP_SERVER_HOST));
        javaMailSender.setPort(configurationRepository.getValueInt(CONFIG_SMTP_SERVER_PORT));
        
        StringBuilderWriter subjectWriter = new StringBuilderWriter();
        new Template("subject_template", emailTemplate.getSubject(), freemarkerConfig).process(templateData, subjectWriter);
        
        StringBuilderWriter bodyWriter = new StringBuilderWriter();
        new Template("body_template", emailTemplate.getText(), freemarkerConfig).process(templateData, bodyWriter);
        
        
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage());
            messageHelper.setFrom(configurationRepository.getValueString(CONFIG_EMAIL_SENDER_DEFAULT));
            messageHelper.setSubject(subjectWriter.toString());
            messageHelper.setText(bodyWriter.toString(), true);
            messageHelper.setTo(contact.getValue());
            javaMailSender.send(messageHelper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("Error sending email notification", e);
        }
        
    }
    
}
