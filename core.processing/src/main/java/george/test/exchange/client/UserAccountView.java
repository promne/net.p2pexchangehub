package george.test.exchange.client;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import de.steinwedel.messagebox.MessageBox;
import es.command.AddEmailContactCommand;
import es.command.AddPhoneNumberContactCommand;
import es.command.RequestContactValidationCodeCommand;
import esw.domain.Offer;
import esw.domain.UserAccount;
import esw.domain.UserAccountContact;
import esw.domain.UserAccountWallet;
import george.test.exchange.client.components.OfferGrid;
import george.test.exchange.client.components.UserAccountGrid;
import george.test.exchange.core.domain.UserAccountRole;

@CDIView(UserAccountView.VIEW_NAME)
public class UserAccountView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "UserAccountView";

    @Inject
    CommandGateway gateway;

    @Inject
    private esw.view.UserAccountView userAccountView;
    
    @Inject
    private OfferGrid offerGrid;

    @Inject
    private UserAccountGrid userAccountGrid;
    
    private VerticalLayout userOverviewLayout = new VerticalLayout();
    
    private BeanItemContainer<UserAccountWallet> userWalletContainer = new BeanItemContainer<>(UserAccountWallet.class);
    private Grid userWalletGrid = new Grid(userWalletContainer);
    
    @PostConstruct
    private void init() {
        setSizeFull();

        TabSheet userDetailTabSheet = new TabSheet();
        userDetailTabSheet.addTab(userOverviewLayout, "Overview");
        userDetailTabSheet.addTab(offerGrid, "Offers");
        userDetailTabSheet.addTab(userWalletGrid, "Wallet");
        
        userDetailTabSheet.setVisible(false);
        
        VerticalSplitPanel panel = new VerticalSplitPanel(userAccountGrid, userDetailTabSheet);        
        panel.setMinSplitPosition(10, Unit.PERCENTAGE);
        panel.setSizeFull();
        
        addComponent(panel);
        
        
        userAccountGrid.addSelectionListener(e -> {
            UserAccount userAccount = userAccountGrid.getSelectedEntity();
            if (userAccount != null) {
                updateUserDetail(userAccount);
            }
            
            userDetailTabSheet.setVisible(userAccount!=null);
        });
        
    }
    
    private void updateUserDetail(UserAccount userAccountShallow) {
        UserAccount userAccount = userAccountView.get(userAccountShallow.getId());
        
        userOverviewLayout.removeAllComponents();
        userOverviewLayout.setSizeFull();
        GridLayout overviewGrid = new GridLayout(2, 1);
        overviewGrid.setSpacing(true);
        overviewGrid.setMargin(true);
//        overviewGrid.setSizeFull();
//        overviewGrid.setColumnExpandRatio(1, 1.0f);
        
        userOverviewLayout.addComponent(overviewGrid);
        
        overviewGrid.addComponent(new Label("ID"));
        overviewGrid.addComponent(new Label(userAccount.getId()));

        overviewGrid.addComponent(new Label("Username"));
        overviewGrid.addComponent(new Label(userAccount.getUsername()));

        overviewGrid.addComponent(new Label("Enabled"));
        overviewGrid.addComponent(new Label(Boolean.toString(userAccount.isEnabled())));

        overviewGrid.addComponent(new Label("Roles"));
        overviewGrid.addComponent(new Label(userAccount.getRoles().stream().map(UserAccountRole::toString).collect(Collectors.joining(", "))));

        GridLayout contactsGrid = new GridLayout(2, 1);
        contactsGrid.setSizeFull();
        contactsGrid.setSpacing(true);
        
        for (UserAccountContact contact : userAccount.getContacts().values()) {
            contactsGrid.addComponent(new Label(contact.getValue()));
            if (!contact.isValidated()) {
                contactsGrid.addComponent(new Button("Send code", c-> {gateway.send(new RequestContactValidationCodeCommand(userAccount.getId(), contact.getId()));}));
            } else {
                contactsGrid.addComponent(new Label("Valid"));                
            }
        }
        
        contactsGrid.addComponent(new Button("Add email", buttonEvent-> {
            TextField emailInputField = new TextField("Email address");
            emailInputField.addValidator(new EmailValidator("Please enter valid email address"));
            MessageBox
                .create()
                .withCaption("Add email address")
                .withMessage(emailInputField)
                .withSaveButton(() -> {
                    emailInputField.validate();
                    gateway.send(new AddEmailContactCommand(userAccount.getId(), emailInputField.getValue()));
                })
                .withCancelButton().open();            
        }));

        contactsGrid.addComponent(new Button("Add mobile phone", buttonEvent-> {
            TextField phoneNumberInputField = new TextField("Phone number");
            MessageBox
                    .create()
                    .withCaption("Add mobile phone number")
                    .withMessage(phoneNumberInputField)
                    .withSaveButton(() -> {
                        phoneNumberInputField.validate();
                        gateway.send(new AddPhoneNumberContactCommand(userAccount.getId(), phoneNumberInputField.getValue()));
                    })
                    .withCancelButton().open();            
        }));
        
        
        overviewGrid.addComponent(new Label("Contacts"));
        overviewGrid.addComponent(contactsGrid);
        
        
        
        offerGrid.getJPAContainerDataSource().removeContainerFilters(Offer.PROPERTY_USER_ACCOUNT_ID);
        offerGrid.getJPAContainerDataSource().addContainerFilter(new Compare.Equal(Offer.PROPERTY_USER_ACCOUNT_ID, userAccount.getId()));
        
        userWalletContainer.removeAllItems();
        userWalletContainer.addAll(userAccount.getWallet());
    }
    
    
    @Override
    public void enter(ViewChangeEvent event) {
        
    }
    
}
