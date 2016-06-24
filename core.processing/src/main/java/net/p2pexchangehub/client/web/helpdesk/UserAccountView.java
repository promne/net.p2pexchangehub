package net.p2pexchangehub.client.web.helpdesk;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.Container.Filter;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.fields.EmailField;

import de.steinwedel.messagebox.MessageBox;
import george.test.exchange.core.domain.UserAccountRole;
import net.p2pexchangehub.client.web.components.OfferGrid;
import net.p2pexchangehub.client.web.components.UserAccountGrid;
import net.p2pexchangehub.client.web.dialog.sendmoney.SendMoneyToUserAccountDialog;
import net.p2pexchangehub.core.api.user.contact.AddEmailContactCommand;
import net.p2pexchangehub.core.api.user.contact.AddPhoneNumberContactCommand;
import net.p2pexchangehub.core.api.user.contact.RequestContactValidationCodeCommand;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact;
import net.p2pexchangehub.view.domain.UserAccountWallet;

@CDIView(UserAccountView.VIEW_NAME)
@RolesAllowed("admin")
public class UserAccountView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "UserAccountView";

    @Inject
    CommandGateway gateway;

    @Inject
    private net.p2pexchangehub.view.repository.UserAccountRepository userAccountRepository;
    
    @Inject
    private OfferGrid offerGrid;
    private Collection<Filter> offerGridAppliedFilters = new HashSet<>();
    
    @Inject
    private UserAccountGrid userAccountGrid;
    
    private VerticalLayout userOverviewLayout = new VerticalLayout();
    
    private BeanItemContainer<UserAccountWallet> userWalletContainer = new BeanItemContainer<>(UserAccountWallet.class);
    private Grid userWalletGrid = new Grid(userWalletContainer);
    private GridContextMenu walletContextMenu = new GridContextMenu(userWalletGrid);
    
    @Inject
    private SendMoneyToUserAccountDialog sendMoneyToUserAccountDialog;
    
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
        UserAccount userAccount = userAccountRepository.findOne(userAccountShallow.getId());
        
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
        
        overviewGrid.addComponent(new Label("Name"));
        overviewGrid.addComponent(new Label(userAccount.getName()));

        overviewGrid.addComponent(new Label("Enabled"));
        overviewGrid.addComponent(new Label(Boolean.toString(userAccount.isEnabled())));

        overviewGrid.addComponent(new Label("Roles"));
        overviewGrid.addComponent(new Label(userAccount.getRoles().stream().map(UserAccountRole::toString).collect(Collectors.joining(", "))));

        overviewGrid.addComponent(new Label("Payments code"));
        overviewGrid.addComponent(new Label(userAccount.getPaymentsCode()));

        GridLayout contactsGrid = new GridLayout(2, 1);
        contactsGrid.setSizeFull();
        contactsGrid.setSpacing(true);
        
        for (UserAccountContact contact : userAccount.getContacts()) {
            contactsGrid.addComponent(new Label(contact.getValue()));
            if (!contact.isValidated()) {
                contactsGrid.addComponent(new Button("Send code", c-> {gateway.send(new RequestContactValidationCodeCommand(userAccount.getId(), contact.getValue()));}));
            } else {
                contactsGrid.addComponent(new Label("Valid"));                
            }
        }
        
        contactsGrid.addComponent(new Button("Add email", buttonEvent-> {
            TextField emailInputField = new EmailField("Email address");
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
        
        
        Collection<Filter> newFilters = Arrays.asList(new Compare.Equal(Offer.PROPERTY_USER_ACCOUNT_ID, userAccount.getId()));
        offerGridAppliedFilters.forEach(offerGrid.getGeneratedPropertyContainer()::removeContainerFilter);
        newFilters.forEach(offerGrid.getGeneratedPropertyContainer()::addContainerFilter);
        offerGridAppliedFilters = newFilters;
        
        userWalletContainer.removeAllItems();
        userWalletContainer.addAll(userAccount.getWallet());
        
        walletContextMenu.addGridBodyContextMenuListener(e -> {
            walletContextMenu.removeItems();
            walletContextMenu.addItem("Send money", c -> {
                sendMoneyToUserAccountDialog.open(userAccount);
            });
        });
        
    }
    
    
    @Override
    public void enter(ViewChangeEvent event) {
        
    }
    
}
