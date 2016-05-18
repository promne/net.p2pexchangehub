package george.test.exchange.client.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.contextmenu.MenuItem;
import com.vaadin.cdi.ViewScoped;

import java.util.Arrays;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.AddUserAccountRolesCommand;
import es.command.DisableUserAccountCommand;
import es.command.EnableUserAccountCommand;
import es.command.RemoveUserAccountRolesCommand;
import esw.domain.UserAccount;
import esw.view.UserAccountView;
import george.test.exchange.core.domain.UserAccountRole;

@ViewScoped
public class UserAccountGrid extends JPAGrid<UserAccount> {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountView userAccountView;

    public UserAccountGrid() {
        super();
        
        setCellStyleGenerator(cellRef -> {
            UserAccount userAccount = getEntity(cellRef.getItemId());
            return userAccount.isEnabled() ? null : THEME_STYLE_WARNING;
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
            }
            
        });        
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        setCaption("Users");
        setColumns(UserAccount.PROPERTY_USERNAME, UserAccount.PROPERTY_ENABLED);        
    }

    @Override
    protected Class<UserAccount> getItemClass() {
        return UserAccount.class;
    }
    
}
