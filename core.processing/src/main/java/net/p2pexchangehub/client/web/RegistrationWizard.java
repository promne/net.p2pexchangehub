package net.p2pexchangehub.client.web;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.RichTextArea;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;
import org.vaadin.viritin.fields.EmailField;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.vaadin.viritin.layouts.MWindow;

import de.steinwedel.messagebox.MessageBox;
import net.p2pexchangehub.client.web.data.StringEqualsValidator;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api.user.AuthenticateUserAccountCommand;
import net.p2pexchangehub.core.api.user.ChangeUserAccountNameCommand;
import net.p2pexchangehub.core.api.user.CreateUserAccountCommand;
import net.p2pexchangehub.core.api.user.EnableUserAccountCommand;
import net.p2pexchangehub.core.api.user.SetUserAccountPasswordCommand;
import net.p2pexchangehub.core.api.user.contact.AddEmailContactCommand;
import net.p2pexchangehub.core.api.user.contact.RequestContactValidationCodeCommand;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact.Type;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class RegistrationWizard extends MWindow {
    
    @Inject
    private CommandGateway commandGateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;
    
    @Inject
    private UserIdentity userIdentity;
    
    private final Item userItem;
    
    private final String PROPERTY_USERNAME = "username";
    private final String PROPERTY_REAL_NAME = "realname";
    private final String PROPERTY_EMAIL = "email";
    private final String PROPERTY_EMAIL_CONFIRM = "email_confirm";
    private final String PROPERTY_PASSWORD = "password";
    private final String PROPERTY_PASSWORD_CONFIRM = "password_confirm";
    
    public RegistrationWizard() {
        super("New user registration");
        setModal(true);
        setWidth("40%");
        setHeight("60%");
        setResizable(false);
        
        userItem = new PropertysetItem();
        userItem.addItemProperty(PROPERTY_USERNAME, new ObjectProperty<>(""));
        userItem.addItemProperty(PROPERTY_REAL_NAME, new ObjectProperty<>(""));
        userItem.addItemProperty(PROPERTY_EMAIL, new ObjectProperty<>(""));
        userItem.addItemProperty(PROPERTY_EMAIL_CONFIRM, new ObjectProperty<>(""));
        userItem.addItemProperty(PROPERTY_PASSWORD, new ObjectProperty<>(""));
        userItem.addItemProperty(PROPERTY_PASSWORD_CONFIRM, new ObjectProperty<>(""));
    }
    
    @PostConstruct
    public void init() {
        Wizard wizard = new Wizard();
        wizard.getFinishButton().setVisible(false);
        wizard.getHeader().setVisible(false);
        
        wizard.addStep(getRegisterStep());
        wizard.addStep(getValidateStep());
        wizard.addStep(getSuccessStep());
        
        wizard.addListener(new WizardProgressListener() {
            
            @Override
            public void wizardCompleted(WizardCompletedEvent event) {
                RegistrationWizard.this.close();
                Page.getCurrent().reload();
            }
            
            @Override
            public void wizardCancelled(WizardCancelledEvent event) {
                RegistrationWizard.this.close();
            }
            
            @Override
            public void stepSetChanged(WizardStepSetChangedEvent event) {
                //nothing
            }
            
            @Override
            public void activeStepChanged(WizardStepActivationEvent event) {
                Wizard wizard = event.getWizard();
                int stepIndex = wizard.getSteps().indexOf(event.getActivatedStep());
                if (stepIndex==wizard.getSteps().size()-1) {
                    wizard.getCancelButton().setVisible(false);
                    wizard.getBackButton().setVisible(false);
                    wizard.getNextButton().setVisible(false);
                    wizard.getFinishButton().setVisible(true);
                }
            }
        });

        MHorizontalLayout horizontalLayout = new MHorizontalLayout(wizard).withMargin(true);
        horizontalLayout.setSizeFull();
        this.setContent(horizontalLayout);
    }

    private WizardStep getRegisterStep() {
        
        return new WizardStep() {

            FieldGroup fg;
            
            @Override
            public boolean onBack() {
                return true;
            }
            
            @Override
            public boolean onAdvance() {
                boolean valid = fg.isValid();
                if (!valid) {
                    MessageBox.createWarning().withCaption("Invalid values").withMessage("Please correct the highlighted fields before going on to the next step").withOkButton().open();
                }
                return valid;
            }
            
            @Override
            public Component getContent() {
                final String fieldWidth = "30em";
                MVerticalLayout stepLayout = new MVerticalLayout();
                
                fg = new FieldGroup(userItem);
                fg.setBuffered(false);
                
                final String usernamePattern = "^[a-zA-Z0-9\\._\\-]{4,16}$";
                MTextField usernameField = new MTextField("Username").withWidth(fieldWidth).withRequired(true).withRequiredError("Username is required")
                        .withValidator(new AbstractStringValidator("Username already exists") {
                            
                            @Override
                            protected boolean isValidValue(String value) {
                                return !userAccountRepository.findOneByUsernameIgnoreCase(value).isPresent();
                            }
                        })
                        .withValidator(new RegexpValidator(usernamePattern, "Username can contain only characters matching the pattern " + usernamePattern))
                        ;
                
                fg.bind(usernameField, PROPERTY_USERNAME);
                
                MTextField realNameField = new MTextField("First name and surname").withWidth(fieldWidth).withRequired(true).withRequiredError("Name is required");
                fg.bind(realNameField, PROPERTY_REAL_NAME);
                
                MTextField emailField = new EmailField("Email").withWidth(fieldWidth).withRequired(true).withValidator(new EmailValidator("Enter a valid email address"));
                fg.bind(emailField, PROPERTY_EMAIL);
                
                MPasswordField passwordField = new MPasswordField("Password").withWidth(fieldWidth).withRequired(true).withRequiredError("Password is required")
                        .withValidator(new StringLengthValidator("Password has to be between 6 and 50 characters", 6, 50, false));
                fg.bind(passwordField, PROPERTY_PASSWORD);
                
                stepLayout.add(usernameField, realNameField, emailField, passwordField);        
                return stepLayout;
            }
            
            @Override
            public String getCaption() {
                return "Basic information";
            }
        };
    }

    private WizardStep getValidateStep() {
        return new WizardStep() {

            FieldGroup fg;
            
            @Override
            public boolean onBack() {
                return true;
            }
            
            @Override
            public boolean onAdvance() {
                boolean valid = fg.isValid();
                if (valid) {
                    String userAccountId = UUID.randomUUID().toString();
                    commandGateway.send(new CreateUserAccountCommand(userAccountId, (String) userItem.getItemProperty(PROPERTY_USERNAME).getValue()));
                    commandGateway.send(new SetUserAccountPasswordCommand(userAccountId, (String) userItem.getItemProperty(PROPERTY_PASSWORD).getValue()));
                    commandGateway.send(new ChangeUserAccountNameCommand(userAccountId, (String) userItem.getItemProperty(PROPERTY_REAL_NAME).getValue()));
                    commandGateway.send(new AddEmailContactCommand(userAccountId, (String) userItem.getItemProperty(PROPERTY_EMAIL).getValue()));
                    commandGateway.send(new EnableUserAccountCommand(userAccountId));
                    commandGateway.send(new RequestContactValidationCodeCommand(userAccountId, (String) userItem.getItemProperty(PROPERTY_EMAIL).getValue()));                    
                    commandGateway.send(new AuthenticateUserAccountCommand(userAccountId, (String) userItem.getItemProperty(PROPERTY_PASSWORD).getValue()));
                    userIdentity.setUserAccountId(userAccountId);
                } else {
                    MessageBox.createWarning().withCaption("Invalid values").withMessage("Please correct the highlighted fields before going on to the next step").withOkButton().open();                    
                }
                return valid;
            }
            
            @Override
            public Component getContent() {
                MVerticalLayout stepLayout = new MVerticalLayout();
                
                final String fieldWidth = "30em";
                
                fg = new FieldGroup(userItem);
                fg.setBuffered(false);
                
                MTextField emailField = new EmailField("Confirm email").withWidth(fieldWidth)
                        .withRequired(true).withRequiredError("Email confirmation is required")
                        .withValidator(new StringEqualsValidator((String) userItem.getItemProperty(PROPERTY_EMAIL).getValue(), "Doesn't match previously entered email address"));
                fg.bind(emailField, PROPERTY_EMAIL_CONFIRM);
                
                MPasswordField passwordField = new MPasswordField("Confirm password").withWidth(fieldWidth)
                    .withRequired(true).withRequiredError("Password confirmation is required")
                    .withValidator(new StringEqualsValidator((String) userItem.getItemProperty(PROPERTY_PASSWORD).getValue(), "Doesn't match previously entered password"));
                fg.bind(passwordField, PROPERTY_PASSWORD_CONFIRM);
                
                stepLayout.add(emailField, passwordField);        
                return stepLayout;
            }
            
            @Override
            public String getCaption() {
                return "Information validation";
            }
        };
    }

    private WizardStep getSuccessStep() {
        return new WizardStep() {
            
            @Override
            public boolean onBack() {
                return false;
            }
            
            @Override
            public boolean onAdvance() {
                return true;
            }
            
            @Override
            public Component getContent() {
                StringBuilder sb = new StringBuilder();
                UserAccount userAccount = userIdentity.getUserAccount().get();
                sb.append("<p>Welcome <b>").append(userAccount.getName()).append("</b></p>");
                sb.append("<p>We have sent an email to your address <b>").append(userAccount.getDefaultContact(Type.EMAIL).get().getValue())
                    .append("</b> . You need to confirm this email address before being able to exchange any money, further instructions are included in the email.</p>");
                
                sb.append("<p>Your username is <b>").append(userAccount.getUsername()).append("</b></p>");
                
                sb.append("<p>Enjoy!</p>");
                
                RichTextArea welcomeMessage = new RichTextArea();
                welcomeMessage.setValue(sb.toString());
                welcomeMessage.setSizeFull();
                welcomeMessage.setReadOnly(true);
                
                
                return new MVerticalLayout(welcomeMessage);
            }
            
            @Override
            public String getCaption() {
                return "User account created";
            }
        };
    }


}
