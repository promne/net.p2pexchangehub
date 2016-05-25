package net.p2pexchangehub.core.handler.user;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.user.ChangeUserAccountPaymentsCode;
import net.p2pexchangehub.core.api.user.UserAccountCreatedEvent;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;
import net.p2pexchangehub.core.util.CodeGenerator;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class UserAccountPaymentsCodeGenerator extends AbstractIgnoreReplayEventHandler {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository repository;

    @EventHandler
    public void handle(UserAccountCreatedEvent event) {
        if (isLive()) {
            final int codeLength = 8;
            String code = CodeGenerator.generateCode(codeLength);
            while (repository.findOneByPaymentsCode(code).isPresent()) {
                code = CodeGenerator.generateCode(codeLength);
            }        
            gateway.send(new ChangeUserAccountPaymentsCode(event.getUserAccountId(), code));            
        }
    }
    
}
