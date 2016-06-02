package net.p2pexchangehub.client.web.helpdesk;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.BeanItem;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.data.mongodb.core.MongoOperations;
import org.tylproject.vaadin.addon.MongoContainer;
import org.vaadin.viritin.fields.MTextArea;
import org.vaadin.viritin.fields.MTextField;

import de.steinwedel.messagebox.MessageBox;
import net.p2pexchangehub.core.api.configuration.ChangeConfigurationItemValueCommand;
import net.p2pexchangehub.core.api.configuration.CreateConfigurationItemCommand;
import net.p2pexchangehub.view.domain.ConfigurationItem;
import net.p2pexchangehub.view.repository.ConfigurationRepository;

@CDIView(ConfigurationView.VIEW_NAME)
@RolesAllowed("admin")
public class ConfigurationView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "ConfigurationView";

    @Inject
    private MongoOperations mongoOperations;

    @Inject
    private CommandGateway commandGateway;
    
    private MongoContainer<ConfigurationItem> configurationItemsContainer;
    
    @Inject
    private ConfigurationRepository configurationRepository;

    private Grid configurationItemsGrid;
    
    @PostConstruct
    private void init() {
        setSizeFull();

        configurationItemsContainer = MongoContainer.Builder.forEntity(ConfigurationItem.class, mongoOperations).build();

        configurationItemsGrid = new Grid("Configuration items", configurationItemsContainer);
        configurationItemsGrid.setSizeFull();
        

        GridContextMenu contextMenu = new GridContextMenu(configurationItemsGrid);
        contextMenu.addGridBodyContextMenuListener(e -> {
            contextMenu.removeItems();
            contextMenu.addItem("Refresh", c -> refreshConfigurationItems());
            contextMenu.addItem("New", c -> {
                editConfigurationItem(null);
            });
            
            BeanItem<ConfigurationItem> item = configurationItemsContainer.getItem(e.getItemId());
            if (item!=null) {
                ConfigurationItem configItem = item.getBean();
                contextMenu.addItem("Edit", c -> {
                    editConfigurationItem(configItem);
                });
            }
            
        });
        
        
        addComponent(configurationItemsGrid);
    }

    private void editConfigurationItem(ConfigurationItem emailTemplate) {
        final MTextField nameField = new MTextField("Name")
                .withFullWidth();
        nameField.setMaxLength(250);

        final MTextArea valueField = new MTextArea("Value")
                .withRows(20);
        
        if (emailTemplate!=null) {
            nameField.setValue(emailTemplate.getName());
            valueField.setValue(emailTemplate.getValue());
        }
        
        MessageBox messageBox = MessageBox.create()
                .withCaption("Edit configuration item")
                .withWidth("50%")
                .withMessage(new VerticalLayout(nameField, valueField))
                .withSaveButton(() -> {
                    if (!configurationRepository.exists(nameField.getValue())) {
                        commandGateway.send(new CreateConfigurationItemCommand(nameField.getValue(), valueField.getValue()));
                    } else {
                        commandGateway.send(new ChangeConfigurationItemValueCommand(nameField.getValue(), valueField.getValue()));                        
                    }
                    refreshConfigurationItems();
                });

        messageBox.withCancelButton().open();         
    }
    
    private void refreshConfigurationItems() {
        //mongocontainer is lazy, we need to trick grid to refresh cached data
        configurationItemsGrid.setSortOrder(new ArrayList<>(configurationItemsGrid.getSortOrder()));
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshConfigurationItems();
    }
    
}
