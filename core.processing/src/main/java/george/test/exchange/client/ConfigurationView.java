package george.test.exchange.client;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitHandler;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.layouts.MHorizontalLayout;

import es.command.ChangeConfigurationItemValueCommand;
import esw.domain.ConfigurationItem;

@CDIView(ConfigurationView.VIEW_NAME)
public class ConfigurationView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "ConfigurationView";

    @PersistenceContext
    private EntityManager em;

    @Inject
    private CommandGateway commandGateway;
    
    private JPAContainer<ConfigurationItem> configurationItemsContainer;
    
    @PostConstruct
    private void init() {
        setSizeFull();

        configurationItemsContainer = JPAContainerFactory.makeBatchable(ConfigurationItem.class, em);
        configurationItemsContainer.setWriteThrough(false);

        Grid configurationItemsGrid= new Grid("Configuration items", configurationItemsContainer);
        configurationItemsGrid.setSizeFull();
        configurationItemsGrid.setEditorEnabled(true);
        configurationItemsGrid.getEditorFieldGroup().addCommitHandler(new CommitHandler() {
            
            @Override
            public void preCommit(CommitEvent commitEvent) throws CommitException {
            }
            
            @Override
            public void postCommit(CommitEvent commitEvent) throws CommitException {
                String configItemName = commitEvent.getFieldBinder().getField(ConfigurationItem.PROPERTY_NAME).getValue().toString();
                String configItemValue = commitEvent.getFieldBinder().getField(ConfigurationItem.PROPERTY_VALUE).getValue().toString();
                commandGateway.send(new ChangeConfigurationItemValueCommand(configItemName, configItemValue));
            }
        });
        
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
