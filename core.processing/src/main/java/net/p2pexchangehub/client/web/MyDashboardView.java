package net.p2pexchangehub.client.web;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.vaadin.viritin.layouts.MWindow;

import net.p2pexchangehub.client.web.components.OfferGrid;
import net.p2pexchangehub.client.web.form.UserBankAccountForm;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api.user.bank.CreateUserBankAccountCommand;
import net.p2pexchangehub.core.api.user.bank.RemoveUserBankAccountCommand;
import net.p2pexchangehub.core.api.user.contact.RequestContactValidationCodeCommand;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccountNumberValidator;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact;
import net.p2pexchangehub.view.domain.UserAccountContact.Type;
import net.p2pexchangehub.view.domain.UserAccountWallet;
import net.p2pexchangehub.view.domain.UserBankAccount;
import net.p2pexchangehub.view.repository.BankAccountRepository;

@CDIView(MyDashboardView.VIEW_NAME)
//@RollesAllowed - accessible by all
public class MyDashboardView extends HorizontalLayout implements View {

    public static final String VIEW_NAME = "MyDashboarView";

    @Inject
    private CommandGateway commandGateway;
        
    @Inject
    private UserIdentity userIdentity;

    @Inject
    private net.p2pexchangehub.view.repository.UserAccountRepository userAccountRepository;
    
    @Inject
    private BankAccountRepository bankAccountRepository;
    
    @Inject
    private OfferGrid offerGrid;
    
    @Inject
    private ExternalBankAccountNumberValidator externalBankAccountNumberValidator;
    
    @PostConstruct
    private void init() {
        setSpacing(true);
        setMargin(true);
        addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
    }
    
    private Component panelBasicInfo(UserAccount userAccount) {
        GridLayout overviewGrid = new GridLayout(2, 1);
        overviewGrid.setSpacing(true);
        overviewGrid.setMargin(true);
        
        overviewGrid.addComponent(new Label("Username"));
        overviewGrid.addComponent(new Label(userAccount.getUsername()));
        
        overviewGrid.addComponent(new Label("Payments code"));
        overviewGrid.addComponent(new Label(userAccount.getPaymentsCode()));

        overviewGrid.addComponent(new Label("Name"));
        overviewGrid.addComponent(new Label(userAccount.getName()));
        
        Panel panel = new Panel("Basic information");
        panel.setIcon(ThemeResources.USER);
        panel.setContent(overviewGrid);
        return panel;
    }

    private Component panelWallet(UserAccount userAccount) {
        BeanContainer<String, UserAccountWallet> walletContainer = new BeanContainer<>(UserAccountWallet.class);
        walletContainer.setBeanIdProperty(UserAccountWallet.PROPERTY_CURRENCY);
        walletContainer.addAll(userAccount.getWallet());
        
        Grid walletGrid = new Grid(walletContainer);
        walletGrid.setWidth("20em");
        walletGrid.setHeight("15em");

        Panel panel = new Panel("Wallet");
        panel.setIcon(ThemeResources.MONEY);
        panel.setContent(walletGrid);        
        return panel;
    }

    private Component panelBankAccounts(UserAccount userAccount) {
        BeanItemContainer<UserBankAccount> bankAccountContainer = new BeanItemContainer<>(UserBankAccount.class);
        bankAccountContainer.addAll(userAccount.getBankAccounts());
        
        Grid bankAccountGrid = new Grid(bankAccountContainer);
        bankAccountGrid.setColumns(UserBankAccount.PROPERTY_CURRENCY, UserBankAccount.PROPERTY_ACCOUNT_NUMBER, UserBankAccount.PROPERTY_OWNER_NAME);
        bankAccountGrid.setHeight("15em");
        
        GridContextMenu bankAccountMenu = new GridContextMenu(bankAccountGrid);
        bankAccountMenu.addGridBodyContextMenuListener(e -> {
            bankAccountMenu.removeItems();

            bankAccountMenu.addItem("New", c -> {
                List<String> availableCurrencies = bankAccountRepository.findByActiveTrue().stream().map(BankAccount::getCurrency).distinct().collect(Collectors.toList());
                UserBankAccountForm bankAccountForm = new UserBankAccountForm(externalBankAccountNumberValidator, availableCurrencies);
                
                Window window = new MWindow("New bank account", bankAccountForm).withModal(true).withResizable(false)
                        .withWidth("30%"); //TODO otherwise goes screen wide
                
                bankAccountForm.setSavedHandler(userBankAccount -> {
                    window.close();
                    commandGateway.sendAndWait(new CreateUserBankAccountCommand(userAccount.getId(), userBankAccount.getCurrency(), userBankAccount.getAccountNumber(), userBankAccount.getOwnerName()));
                    bankAccountContainer.addBean(userBankAccount);
                });
                
                bankAccountForm.setResetHandler(offer -> {
                    window.close();            
                });
                
                bankAccountForm.setEntity(new UserBankAccount());
                getUI().addWindow(window);
                
            });
            if (e.getItemId()!=null) {
                UserBankAccount bankAccount = (UserBankAccount) e.getItemId();
                bankAccountMenu.addItem("Remove", c -> {
                    commandGateway.send(new RemoveUserBankAccountCommand(userAccount.getId(), bankAccount.getCurrency(), bankAccount.getAccountNumber()));
                });
            }
        });
        
        
        
        Panel panel = new Panel("Bank accounts");
        panel.setIcon(ThemeResources.BANK);
        panel.setContent(bankAccountGrid);        
        return panel;
    }

    private Component panelOffers(UserAccount userAccount) {
        offerGrid.setColumns(OfferGrid.PROPERTY_AMOUNT_OFFERED_READABLE, OfferGrid.PROPERTY_EXCHANGE_RATE_READABLE, Offer.PROPERTY_CURRENCY_OFFERED, Offer.PROPERTY_CURRENCY_REQUESTED, 
                Offer.PROPERTY_STATE);
        offerGrid.getGeneratedPropertyContainer().removeAllContainerFilters();
        offerGrid.getGeneratedPropertyContainer().addContainerFilter(new Compare.Equal(Offer.PROPERTY_USER_ACCOUNT_ID, userAccount.getId()));
        offerGrid.setWidth("70em");
        
        Panel panel = new Panel("Offers");
        panel.setIcon(ThemeResources.EXCHANGE);
        panel.setContent(offerGrid);        
        return panel;
    }
    
    private Component panelContactsInfo(UserAccount userAccount) {
        
        MVerticalLayout layout = new MVerticalLayout();
        
        layout.addComponent(new MLabel("Email"));
        Set<UserAccountContact> emailContacts = userAccount.getContacts(Type.EMAIL);
        if (!emailContacts.isEmpty()) {
            GridLayout emailContactwGrid = new GridLayout(2, 1);
            emailContactwGrid.setSpacing(true);
            
            emailContacts.forEach(mc -> {
                emailContactwGrid.addComponent(new Label(mc.getValue()));
                Component mobileContactAction = null;
                if (!mc.isValidated()) {
                    mobileContactAction = new Button("Validate", c-> {commandGateway.send(new RequestContactValidationCodeCommand(userAccount.getId(), mc.getValue()));});
                } else {
                    MenuBar actionsMenuBar = new MenuBar();
                    MenuItem topItem = actionsMenuBar.addItem("Edit", null);
                    topItem.addItem("Change", null);
                    topItem.addItem("Remove", null);
//                    mobileContactAction = actionsMenuBar; no change atm
                }
                if (mobileContactAction!=null) {
                    emailContactwGrid.addComponent(mobileContactAction);
                }
             });               
            layout.add(emailContactwGrid);
        }

//        layout.addComponent(new Label("Mobile phone"));
//        Set<UserAccountContact> mobileContacts = userAccount.getContacts(Type.PHONE);
//        if (!mobileContacts.isEmpty()) {
//            GridLayout mobileContactwGrid = new GridLayout(2, 1);
//            mobileContactwGrid.setSpacing(true);
//            mobileContacts.forEach(mc -> {
//               mobileContactwGrid.addComponent(new Label(mc.getValue()));
//               Component mobileContactAction = null;
//               if (!mc.isValidated()) {
//                   mobileContactAction = new Button("Validate", c-> {commandGateway.send(new RequestContactValidationCodeCommand(userAccount.getId(), mc.getValue()));});
//               } else {
//                   MenuBar actionsMenuBar = new MenuBar();
//                   MenuItem topItem = actionsMenuBar.addItem("Edit", null);
//                   topItem.addItem("Change", null);
//                   topItem.addItem("Remove", null);
//                   mobileContactAction = actionsMenuBar;
//               }
//               mobileContactwGrid.addComponent(mobileContactAction);
//            });            
//            layout.add(mobileContactwGrid);
//        }
//        MenuBar addMenuBar = new MenuBar();
//        MenuItem addItem = addMenuBar.addItem("Add", null);
//        addItem.addItem("Email", null);
//        addItem.addItem("Mobile phone", null);
//        
//        layout.add(addMenuBar);
        
        Panel panel = new Panel("Contacts");
        panel.setIcon(ThemeResources.PHONE);
        panel.setContent(layout);
        return panel;
    }
    
    
    private void updateUserDetails() {
        this.removeAllComponents();
        
        UserAccount userAccount = userAccountRepository.findOne(userIdentity.getUserAccountId());
        
        addComponent(panelBasicInfo(userAccount));
        addComponent(panelContactsInfo(userAccount));
        addComponent(panelWallet(userAccount));        
        addComponent(panelBankAccounts(userAccount));        
        addComponent(panelOffers(userAccount));        
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        updateUserDetails();
    }
    
}
