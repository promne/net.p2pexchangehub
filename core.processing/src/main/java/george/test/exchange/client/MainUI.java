package george.test.exchange.client;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.cdi.URLMapping;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.replay.ReplayingCluster;

import de.steinwedel.messagebox.MessageBox;

@CDIUI("")
@URLMapping("helpdesk/*")
@Theme(value = "mytheme")
@Widgetset("george.test.exchange.client.MyAppWidgetset")
public class MainUI extends UI {

    @Inject
    private CDIViewProvider viewProvider;
    
    @Inject
    private ReplayingCluster cluster;
    
    @Inject
    private EventBus eventBus;
    
    
    @Override
    protected void init(VaadinRequest request) {
        MenuBar menuBar = new MenuBar();
        
        menuBar.addItem("Accounts", e -> getUI().getNavigator().navigateTo(BankAccountView.VIEW_NAME));
        menuBar.addItem("Offers", e -> getUI().getNavigator().navigateTo(OfferView.VIEW_NAME));
        menuBar.addItem("Users", e -> getUI().getNavigator().navigateTo(UserAccountView.VIEW_NAME));
        menuBar.addItem("Configuration", e -> getUI().getNavigator().navigateTo(ConfigurationView.VIEW_NAME));
        menuBar.addItem("Rebuild view", e -> {
            MessageBox.create()
                .withCaption("Rebuild view")
                .withMessage("Do you really want to trigger view rebuild?")
                .withOkButton(() -> {
//                    ReplayingCluster replayingCluster = new ReplayingCluster(
//                            new SimpleCluster("default"),
//                            eventStoreManagement,
//                            new NoTransactionManager(),
//                            0,
//                            new BackloggingIncomingMessageHandler());
//                    
//                    AnnotationEventListenerAdapter.subscribe(annotatedEventListener, eventBus)
                    eventBus.toString(); //just to trigger axon autoconfigure if it's a first time
                    cluster.startReplay();
                })
                .withCancelButton()
                .open();
        });
        
        
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.addComponent(menuBar);
        
        Panel contentPanel = new Panel();
        contentPanel.setSizeFull();
        mainLayout.addComponent(contentPanel);
        mainLayout.setExpandRatio(contentPanel, 1.0f);

        setContent(mainLayout);
        
        Navigator navigator = new Navigator(this, contentPanel);
        navigator.addProvider(viewProvider);
        navigator.navigateTo(BankAccountView.VIEW_NAME);        
    }

}
