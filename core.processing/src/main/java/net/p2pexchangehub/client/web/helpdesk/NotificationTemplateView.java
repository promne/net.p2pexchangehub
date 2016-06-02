package net.p2pexchangehub.client.web.helpdesk;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.AbstractBeanContainer.BeanIdResolver;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.fields.MTextArea;
import org.vaadin.viritin.fields.MTextField;

import de.steinwedel.messagebox.MessageBox;
import net.p2pexchangehub.core.api.notification.CreateNotificationTemplateCommand;
import net.p2pexchangehub.core.api.notification.UpdateEmailTemplateTextCommand;
import net.p2pexchangehub.view.domain.NotificationTemplateEmail;
import net.p2pexchangehub.view.repository.NotificationTemplateRepository;

@CDIView(NotificationTemplateView.VIEW_NAME)
@RolesAllowed("admin")
public class NotificationTemplateView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "NotificationTemplateView";
    
    private BeanContainer<String, NotificationTemplateEmail> emailTemplateContainer = new BeanContainer<>(NotificationTemplateEmail.class);
    private Grid templateGrid;
    
    @Inject
    private NotificationTemplateRepository templateRepository;
    
    @Inject
    private CommandGateway gateway;
    
    @PostConstruct
    private void init() {
        setSizeFull();
        
        emailTemplateContainer.setBeanIdResolver(new BeanIdResolver<String, NotificationTemplateEmail>() {
            @Override
            public String getIdForBean(NotificationTemplateEmail bean) {
                return bean.getTemplateId()+"//"+bean.getLanguageCode();
            }
        });
        templateGrid = new Grid("Email notification templates", emailTemplateContainer);
        templateGrid.setSizeFull();
        
        GridContextMenu contextMenu = new GridContextMenu(templateGrid);
        contextMenu.addGridBodyContextMenuListener(e -> {
            contextMenu.removeItems();
            contextMenu.addItem("Refresh", c -> refreshTemplates());
            contextMenu.addItem("New", c -> {
                editEmailTemplate(null);
            });
            
            BeanItem<NotificationTemplateEmail> item = emailTemplateContainer.getItem(e.getItemId());
            if (item!=null) {
                NotificationTemplateEmail template = item.getBean();
                contextMenu.addItem("Edit", c -> {
                    editEmailTemplate(template);
                });
            }
            
        });
        
        
        addComponent(templateGrid);
    }
    
    private void editEmailTemplate(NotificationTemplateEmail emailTemplate) {
        final MTextField templateIdField = new MTextField("Template id")
                .withFullWidth();
        templateIdField.setMaxLength(250);

        final MTextField languageCodeField = new MTextField("Language code")
                .withFullWidth();
        languageCodeField.setMaxLength(50);

        final MTextArea subjectField = new MTextArea("Subject")
                .withRows(5);

        final MTextArea textField = new MTextArea("Text")
                .withRows(20);
        
        if (emailTemplate!=null) {
            templateIdField.setValue(emailTemplate.getTemplateId());
            languageCodeField.setValue(emailTemplate.getLanguageCode());
            subjectField.setValue(emailTemplate.getSubject());
            textField.setValue(emailTemplate.getText());
        }
        
        MessageBox messageBox = MessageBox.create()
                .withCaption("Login")
                .withWidth("50%")
                .withMessage(new VerticalLayout(templateIdField, languageCodeField, subjectField, textField))
                .withSaveButton(() -> {
                    if (!templateRepository.exists(templateIdField.getValue())) {
                        gateway.send(new CreateNotificationTemplateCommand(templateIdField.getValue()));
                    } 
                    gateway.send(new UpdateEmailTemplateTextCommand(templateIdField.getValue(), languageCodeField.getValue(), subjectField.getValue(), textField.getValue()));
                    refreshTemplates();
                });

        messageBox.withCancelButton().open();         
    }
    
    private void refreshTemplates() {
        List<NotificationTemplateEmail> emailTemplates = templateRepository.findAll().stream().flatMap(m -> m.getEmailTemplates().values().stream()).collect(Collectors.toList());
        emailTemplateContainer.removeAllItems();
        emailTemplateContainer.addAll(emailTemplates);
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshTemplates();
    }

}
