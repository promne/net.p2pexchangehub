package net.p2pexchangehub.client.web.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.contextmenu.MenuItem;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.data.Item;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.ui.TextField;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import de.steinwedel.messagebox.MessageBox;
import george.test.exchange.core.domain.UserAccountRole;
import net.p2pexchangehub.core.api.user.AddUserAccountRolesCommand;
import net.p2pexchangehub.core.api.user.ChangeUserAccountNameCommand;
import net.p2pexchangehub.core.api.user.DisableUserAccountCommand;
import net.p2pexchangehub.core.api.user.EnableUserAccountCommand;
import net.p2pexchangehub.core.api.user.RemoveUserAccountRolesCommand;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact;
import net.p2pexchangehub.view.domain.UserAccountContact.Type;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@ViewScoped
public class UserAccountGrid extends MongoGrid<UserAccount> {

    public static final String PROPERTY_EMAILS = "emails";
    
    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;

    public UserAccountGrid() {
        super(UserAccount.class);
        
        setCellStyleGenerator(cellRef -> {            
            UserAccount userAccount = getEntity(cellRef.getItemId());
            String cellStyle = null;
            if (cellRef.getPropertyId().equals(PROPERTY_EMAILS) && (!userAccount.getContacts(Type.EMAIL).stream().allMatch(UserAccountContact::isValidated))) {
                cellStyle = THEME_STYLE_WARNING;
            }
            if (!userAccount.isEnabled()) {
                cellStyle = THEME_STYLE_WARNING;
            }
            return cellStyle;
        } );        
        
        
        GridContextMenu userAccountContextMenu = getContextMenu();
        getContextMenu().addGridBodyContextMenuListener(e -> {
            userAccountContextMenu.removeItems();
            userAccountContextMenu.addItem("Refresh", c -> refresh());
            
            UserAccount userAccount = getEntity(e.getItemId());
            if (userAccount!=null) {
                MenuItem enableUserItem = userAccountContextMenu.addItem("Enabled", c -> {
                    if (c.isChecked()) {
                        gateway.send(new EnableUserAccountCommand(userAccount.getId()));
                    } else {
                        gateway.send(new DisableUserAccountCommand(userAccount.getId()));                        
                    }
                });
                enableUserItem.setCheckable(true);
                enableUserItem.setChecked(userAccount.isEnabled());
                
                MenuItem rolesItem = userAccountContextMenu.addItem("Roles", null);
                for (UserAccountRole role : UserAccountRole.values()) {
                    MenuItem singleRoleItem = rolesItem.addItem(role.toString(), c -> {
                        if (c.isChecked()) {
                            gateway.send(new AddUserAccountRolesCommand(userAccount.getId(), new HashSet<>(Arrays.asList(role))));
                        } else {
                            gateway.send(new RemoveUserAccountRolesCommand(userAccount.getId(), new HashSet<>(Arrays.asList(role))));
                        }                        
                    });
                    singleRoleItem.setCheckable(true);
                    singleRoleItem.setChecked(userAccount.getRoles().contains(role));
                }
                
                userAccountContextMenu.addItem("Change name", c -> {
                    TextField nameInputField = new TextField("Name", userAccount.getName());
                    MessageBox
                        .create()
                        .withCaption("Change name")
                        .withMessage(nameInputField)
                        .withSaveButton(() -> {
                            nameInputField.validate();
                            gateway.send(new ChangeUserAccountNameCommand(userAccount.getId(), nameInputField.getValue()));
                            userAccount.setName(nameInputField.getValue());
                        })
                        .withCancelButton().open();            
                });
            }
            
        });        
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        setCaption("Users");

        getGeneratedPropertyContainer().addGeneratedProperty(PROPERTY_EMAILS, new PropertyValueGenerator<String>() {
            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                UserAccount userAccount = getEntity(itemId);
                return userAccount.getContacts(Type.EMAIL).stream().map(UserAccountContact::getValue).collect(Collectors.joining(" "));
            }
            
            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        
        
        setColumns(UserAccount.PROPERTY_ID, UserAccount.PROPERTY_USERNAME, UserAccount.PROPERTY_PAYMENTS_CODE, UserAccount.PROPERTY_NAME, UserAccount.PROPERTY_ROLES, UserAccount.PROPERTY_ENABLED, UserAccount.PROPERTY_WALLET, PROPERTY_EMAILS);
        setVisibleColumns(UserAccount.PROPERTY_USERNAME, PROPERTY_EMAILS, UserAccount.PROPERTY_ROLES, UserAccount.PROPERTY_WALLET);
    }

    @Override
    protected Class<UserAccount> getItemClass() {
        return UserAccount.class;
    }
    
}
