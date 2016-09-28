package net.p2pexchangehub.core.handler.user;

import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.user.AddUserAccountRolesCommand;
import net.p2pexchangehub.core.api.user.contact.ContactDetailValidatedEvent;
import net.p2pexchangehub.core.domain.UserAccountRole;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class UserAccountRolePromoter extends AbstractIgnoreReplayEventHandler {

    @Inject
    private CommandGateway commandGateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;
    
    @EventHandler
    public void handlePromoteToTraderOnContactValidation(ContactDetailValidatedEvent event) {
        if (isLive()) {
            UserAccount userAccount = userAccountRepository.findOne(event.getUserAccountId());
            if (!userAccount.getRoles().contains(UserAccountRole.TRADER)) {
                commandGateway.send(new AddUserAccountRolesCommand(event.getUserAccountId(), new HashSet<>(Arrays.asList(UserAccountRole.TRADER))));
            }
        }
    }
    
}
