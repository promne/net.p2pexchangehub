package net.p2pexchangehub.client.web;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Panel;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.ui.MNotification;

import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api.user.contact.ValidateContactDetailCommand;

@CDIView(value = EmailConfirmationView.VIEW_NAME, supportsParameters=true)
//@RollesAllowed - accessible by all
public class EmailConfirmationView extends Panel implements View {
    
    public static final String VIEW_NAME = "EmailConfirmationView";

    @Inject
    private CommandGateway commandGateway;
    
    @Inject
    private UserIdentity userIdentity;
    
    @Override
    public void enter(ViewChangeEvent event) {
        String validatingCode = event.getParameters();        
        commandGateway.send(new ValidateContactDetailCommand(userIdentity.getUserAccountId(), validatingCode));
        MNotification.tray("Email contact validated");
        event.getNavigator().navigateTo("");
    }

}
