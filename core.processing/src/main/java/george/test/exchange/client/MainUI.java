package george.test.exchange.client;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

@CDIUI("")
@Theme(value = "mytheme")
public class MainUI extends UI {

    @Inject
    private CDIViewProvider viewProvider;
    
    @Override
    protected void init(VaadinRequest request) {
        MenuBar menuBar = new MenuBar();
        
        menuBar.addItem("Accounts", e -> getUI().getNavigator().navigateTo(AccountsView.VIEW_NAME));
        menuBar.addItem("Offers", e -> getUI().getNavigator().navigateTo(OffersView.VIEW_NAME));
        menuBar.addItem("Configuration", e -> getUI().getNavigator().navigateTo(ConfigurationView.VIEW_NAME));
        
        
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
        navigator.navigateTo(AccountsView.VIEW_NAME);        
    }

}
