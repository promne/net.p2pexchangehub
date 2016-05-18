package es.command.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;

import es.aggregate.UserAccount;
import es.command.AddEmailContactCommand;
import es.command.AddPhoneNumberContactCommand;
import es.command.AddUserAccountRolesCommand;
import es.command.CreateUserAccountCommand;
import es.command.CreateUserBankAccountCommand;
import es.command.DisableUserAccountCommand;
import es.command.EnableUserAccountCommand;
import es.command.RemoveUserAccountRolesCommand;
import es.command.RequestContactValidationCodeCommand;
import es.command.SetUserAccountPasswordCommand;
import es.command.ValidateContactDetailCommand;
import esw.view.UserAccountView;

@Singleton
public class UserAccountCommandHandler {

    @Inject
    private Logger log;
    
    @Inject
    private Repository<UserAccount> repository;
    
    @Inject
    private UserAccountView userAccountView;
    
    @CommandHandler
    public void handleCreateUserAccount(CreateUserAccountCommand command) {
        Optional<esw.domain.UserAccount> existingUserAccount = userAccountView.getByUsername(command.getUsername());
        if (existingUserAccount.isPresent()) {
            log.debug("Can't create user {} - this username already exists", command.getUsername());
        } else {
            UserAccount userAccount = new UserAccount(command.getUserAccountId(), command.getUsername());
            repository.add(userAccount);
        }
    }
    
    @CommandHandler
    public void handleCreateBankAccount(CreateUserBankAccountCommand command) {
        UserAccount userAccount = repository.load(command.getUserAccountId());
        userAccount.createBankAccount(command.getCurrency(), command.getAccountNumber());
    }
    
    @CommandHandler
    public void handleSetUserAccountPassword(SetUserAccountPasswordCommand command) {
        UserAccount userAccount = repository.load(command.getUserAccountId());
        String passwordHash = BCrypt.hashpw(command.getPassword(), BCrypt.gensalt(5));
        userAccount.setPasswordHash(passwordHash);
    }

    @CommandHandler
    public void handleEnableUserAccount(EnableUserAccountCommand command) {
        repository.load(command.getUserAccountId()).enable();
    }

    @CommandHandler
    public void handleDisableUserAccount(DisableUserAccountCommand command) {
        repository.load(command.getUserAccountId()).disable();        
    }

    @CommandHandler
    public void handleAddRoles(AddUserAccountRolesCommand command) {
        repository.load(command.getUserAccountId()).addRoles(command.getRoles());        
    }

    @CommandHandler
    public void handleRemoveRoles(RemoveUserAccountRolesCommand command) {
        repository.load(command.getUserAccountId()).removeRoles(command.getRoles());        
    }

    @CommandHandler
    public void handleAddEmailContact(AddEmailContactCommand command) {
        repository.load(command.getUserAccountId()).addEmailContact(command.getEmail());
    }

    @CommandHandler
    public void handleAddPhoneNumberContact(AddPhoneNumberContactCommand command) {
        repository.load(command.getUserAccountId()).addPhoneNumberContact(command.getPhoneNumber());
    }

    @CommandHandler
    public void handleRequestValidationCode(RequestContactValidationCodeCommand command) {
        UserAccount userAccount = repository.load(command.getUserAccountId());
        String validationCode = ""; //TODO generate code
        Date expiration = Date.from(Instant.now().plus(Duration.ofHours(2)));
        userAccount.requestValidationCode(command.getContactId(), validationCode, expiration);
    }
    
    @CommandHandler
    public void handleValidateContact(ValidateContactDetailCommand command) {
        repository.load(command.getUserAccountId()).validateContact(command.getContactId(), command.getValidatingCode());        
    }
    
}
