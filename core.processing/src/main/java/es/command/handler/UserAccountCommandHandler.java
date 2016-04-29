package es.command.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;

import es.aggregate.UserAccount;
import es.command.CreateUserAccountCommand;

@Singleton
public class UserAccountCommandHandler {

    @Inject
    private Logger log;
    
    @Inject
    private Repository<UserAccount> repository;
    
    @CommandHandler
    public void handleCreateExternalBankAccount(CreateUserAccountCommand command) {
        UserAccount userAccount = new UserAccount(command.getUserAccountId(), command.getUsername());
        repository.add(userAccount);
    }
    
}
