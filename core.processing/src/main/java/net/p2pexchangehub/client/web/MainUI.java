package net.p2pexchangehub.client.web;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.URLMapping;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.util.Optional;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.replay.ReplayingCluster;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.button.PrimaryButton;
import org.vaadin.viritin.fields.MPasswordField;
import org.vaadin.viritin.fields.MTextField;

import de.steinwedel.messagebox.ButtonOption;
import de.steinwedel.messagebox.MessageBox;
import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.processing.service.AuthenticationService;
import net.p2pexchangehub.client.web.dialog.sendmoney.SendMoneyToUserAccountDialog;
import net.p2pexchangehub.client.web.helpdesk.BankAccountView;
import net.p2pexchangehub.client.web.helpdesk.ConfigurationView;
import net.p2pexchangehub.client.web.helpdesk.NotificationTemplateView;
import net.p2pexchangehub.client.web.helpdesk.OfferView;
import net.p2pexchangehub.client.web.helpdesk.SessionView;
import net.p2pexchangehub.client.web.helpdesk.UserAccountView;
import net.p2pexchangehub.client.web.offermatch.OfferMatchView;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.client.web.tools.CDIViewProvider;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.BankAccountRepository;
import net.p2pexchangehub.view.repository.BankAccountRepositoryHelper;

@CDIUI("")
@URLMapping("client/*")
@Theme(value = "mytheme")
@Widgetset(ThemeResources.WIDGETSET_NAME)
public class MainUI extends UI {

    @Inject
    private CDIViewProvider viewProvider;
    
    @Inject
    private ReplayingCluster cluster;
    
    @Inject
    private EventBus eventBus;
    
    @Inject
    private UserIdentity userIdentity;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private SendMoneyToUserAccountDialog sendMoneyToUserAccountDialog;
    
    @Inject
    private BankAccountRepositoryHelper bankAccountRepositoryHelper;
    
    @Inject
    private BankAccountRepository bankAccountRepository;
    
    @Override
    protected void init(VaadinRequest request) {
        if (userIdentity.isLoggeedIn()) {
            loadProtectedResources(request);
        } else {
            MTextField usernameField = new MTextField("Username").withFullWidth();
            usernameField.setMaxLength(50);

            MPasswordField passwordField = new MPasswordField("Password").withFullWidth();
            passwordField.setMaxLength(50);
            
            final MessageBox[] messageBox = new MessageBox[1];
            messageBox[0] = MessageBox
            .create()
            .withCaption("Login")
            .withMessage(new VerticalLayout(usernameField, passwordField))
            .withButton(new PrimaryButton(ThemeResources.UNLOCK, "Login", c -> {
                Optional<UserAccount> user = authenticationService.authenticate(usernameField.getValue(), passwordField.getValue());
                if (user.isPresent()) {
                    userIdentity.setUserAccountId(user.get().getId());
                    loadProtectedResources(request);
                    messageBox[0].close();
                } else {
                    passwordField.clear();
                    Notification.show("Username and password doesn't match", Type.ERROR_MESSAGE);
                }
            }), ButtonOption.closeOnClick(false))
            .withButton(new MButton(ThemeResources.USER_ADD, "Register", c-> {
                messageBox[0].close();
                UI.getCurrent().addWindow(CDI.current().select(RegistrationWizard.class).get());
            }), ButtonOption.closeOnClick(false));
            messageBox[0].open();                                
            
        }
    }
    
    protected void loadProtectedResources(VaadinRequest request) {
        MenuBar menuBar = new MenuBar();
        
        addTopNavigation(menuBar, "Dashboard", ThemeResources.HOME, MyDashboardView.class);
        addTopNavigationIWantTo(menuBar);
        addTopNavigation(menuBar, "Bank accounts", BankAccountView.class);
        addTopNavigation(menuBar, "Offers", OfferView.class);
        addTopNavigation(menuBar, "Users", UserAccountView.class);
        addTopNavigation(menuBar, "Configuration", ConfigurationView.class);
        addTopNavigation(menuBar, "Notifications", NotificationTemplateView.class);
        addTopNavigation(menuBar, "Sessions", SessionView.class);

        if (userIdentity.hasRole(UserAccountRole.ADMIN)) {
            menuBar.addItem("Rebuild view", e -> {
                MessageBox.create()
                .withCaption("Rebuild view")
                .withMessage("Do you really want to trigger view rebuild?")
                .withOkButton(() -> {
                    eventBus.toString(); //just to trigger axon autoconfigure if it's a first time
                    //TODO: split clusters based on their function to avoid replaying commands generating ones.
                    cluster.startReplay();
                })
                .withCancelButton()
                .open();
            });            
        }
        
        menuBar.addItem("Logout", ThemeResources.SIGN_OUT, e -> {
            userIdentity.logout();
            UI.getCurrent().setContent(null);
            Page.getCurrent().reload();
        });
        
                
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.addComponent(menuBar);
        
        Panel contentPanel = new Panel();
        contentPanel.setSizeFull();
        mainLayout.addComponent(contentPanel);
        mainLayout.setExpandRatio(contentPanel, 1.0f);

        setContent(mainLayout);
        
        viewProvider.setDefaultView(MyDashboardView.class);
        Navigator navigator = new Navigator(this, contentPanel);
        navigator.addProvider(viewProvider);
        navigator.navigateTo(navigator.getState());
    }

    
    private void addTopNavigationIWantTo(MenuBar menuBar) {
        MenuItem actionsMenu = menuBar.addItem("I want to ...", FontAwesome.GEARS, null);
        
        actionsMenu.addItem("Exchange money", c -> getNavigator().navigateTo(OfferMatchView.VIEW_NAME));
        actionsMenu.addItem("Send money to my bank account", c -> iWantToSendMoneyToBank());
        
        MenuItem menuItemChargeWallet = actionsMenu.addItem("Charge my wallet from my bank account", null);
        bankAccountRepositoryHelper.listAvailableCurrencies().forEach(currency -> {
            menuItemChargeWallet.addItem(currency, c -> iWantToTopupWallet(currency));            
        });
    }

    private void iWantToTopupWallet(String currency) {
        String chargeWalletTemplate = 
                "<p>To charge your wallet with %1$s you need to send money to one of our trust accounts."
                + " Once we receive your payment you will be able to exchange them for a different currency.</p>"
                + "<p>You can use following account:</p>"
                + "<p><center><strong>%2$s</strong></center></p>"
                + "<p>For us to recognize it's a payment from you, please include your payment code <strong>%3$s</strong> as a reference.</p>"
                ;
        
        
        String incomingPaymentInstructions = bankAccountRepositoryHelper.getIncomingPaymentInstructions(currency, userIdentity.getUserAccountId());
        String chargeWalletMessage = String.format(chargeWalletTemplate, currency, incomingPaymentInstructions, userIdentity.getUserAccount().get().getPaymentsCode());
        MessageBox.create()
            .withCaption("Charge your wallet")
            .withHtmlMessage(chargeWalletMessage)
            .withButton(new PrimaryButton("OK")).asModal(true).open();
        
    }

    private void iWantToSendMoneyToBank() {
        if (userIdentity.getUserAccount().get().getWallet().stream().anyMatch(w -> w.getAmount().signum()==1)) {
            sendMoneyToUserAccountDialog.open(userIdentity.getUserAccount().get());
        } else {
            MessageBox.createWarning().asModal(true).withCaption("Error sending money").withMessage("Your wallet is empty, you can't send any money to any bank account.").withButton(new PrimaryButton("OK")).open();
        }
    }

    protected MenuItem addTopNavigation(MenuBar menuBar, String caption, Resource icon, Class<? extends View> viewClass) {
        MenuItem result = null;
        if (viewProvider.isUserHavingAccessToView(viewClass)) {
            String viewName = viewProvider.getViewNameFromAnnotation(viewClass);
            result = menuBar.addItem(caption, icon, e -> getUI().getNavigator().navigateTo(viewName));
        }
        return result;
        
    }
    
    protected MenuItem addTopNavigation(MenuBar menuBar, String caption, Class<? extends View> viewClass) {
        return addTopNavigation(menuBar, caption, null, viewClass);
    }
}
