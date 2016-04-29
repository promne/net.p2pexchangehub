package george.test.exchange.client;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.vaadin.viritin.layouts.MHorizontalLayout;

import esw.domain.ConfigurationItem;

@CDIView(ConfigurationView.VIEW_NAME)
public class ConfigurationView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "ConfigurationView";

    @PersistenceContext
    private EntityManager em;

    private JPAContainer<ConfigurationItem> configurationItemsContainer;
    
    @PostConstruct
    private void init() {
        setSizeFull();

        configurationItemsContainer = JPAContainerFactory.make(ConfigurationItem.class, em);
        configurationItemsContainer.setReadOnly(true);

        Grid configurationItemsGrid= new Grid("Configuration items", configurationItemsContainer);
        configurationItemsGrid.setSizeFull();
        addComponent(configurationItemsGrid);
        setExpandRatio(configurationItemsGrid, 1.0f);

        MHorizontalLayout c = new MHorizontalLayout();
        addComponent(c);
        c.addComponent(new Button("Refresh", e -> refreshConfigurationItems()));
    }
        
    private void refreshConfigurationItems() {
        configurationItemsContainer.refresh();
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshConfigurationItems();
    }
    
}
