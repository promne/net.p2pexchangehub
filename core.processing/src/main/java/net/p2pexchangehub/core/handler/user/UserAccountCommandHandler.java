package net.p2pexchangehub.core.handler.user;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.MetaData;
import org.axonframework.repository.Repository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.user.AddUserAccountRolesCommand;
import net.p2pexchangehub.core.api.user.ChangeUserAccountPaymentsCode;
import net.p2pexchangehub.core.api.user.ConfirmAccountDebitReservationCommand;
import net.p2pexchangehub.core.api.user.CreateUserAccountCommand;
import net.p2pexchangehub.core.api.user.CreditOfferFromUserAccountCommand;
import net.p2pexchangehub.core.api.user.CreditUserAccountFromDeclinedOfferCommand;
import net.p2pexchangehub.core.api.user.DisableUserAccountCommand;
import net.p2pexchangehub.core.api.user.DiscardAccountDebitReservationCommand;
import net.p2pexchangehub.core.api.user.EnableUserAccountCommand;
import net.p2pexchangehub.core.api.user.RemoveUserAccountRolesCommand;
import net.p2pexchangehub.core.api.user.RequestSendNotificationToUserAccountCommand;
import net.p2pexchangehub.core.api.user.SendMoneyToUserBankAccountCommand;
import net.p2pexchangehub.core.api.user.SetUserAccountPasswordCommand;
import net.p2pexchangehub.core.api.user.bank.CreateUserBankAccountCommand;
import net.p2pexchangehub.core.api.user.contact.AddEmailContactCommand;
import net.p2pexchangehub.core.api.user.contact.AddPhoneNumberContactCommand;
import net.p2pexchangehub.core.api.user.contact.RequestContactValidationCodeCommand;
import net.p2pexchangehub.core.api.user.contact.ValidateContactDetailCommand;
import net.p2pexchangehub.core.handler.offer.ExchangeOffer;
import net.p2pexchangehub.core.handler.user.ContactDetail.Type;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@Singleton
public class UserAccountCommandHandler {

    @Inject
    private Logger log;
    
    @Inject
    private Repository<UserAccount> repository;

    @Inject
    private Repository<ExchangeOffer> offerAggregateRepository;
    
    @Inject
    private UserAccountRepository userAccountRepository;
    
    @CommandHandler
    public void handleCreateUserAccount(CreateUserAccountCommand command, MetaData metadata) {
        Optional<net.p2pexchangehub.view.domain.UserAccount> existingUserAccount = userAccountRepository.findOneByUsername(command.getUsername());
        if (existingUserAccount.isPresent()) {
            log.debug("Can't create user {} - this username already exists", command.getUsername());
        } else {
            UserAccount userAccount = new UserAccount(command.getUserAccountId(), command.getUsername(), metadata);
            repository.add(userAccount);
        }
    }
    
    @CommandHandler
    public void handleCreateBankAccount(CreateUserBankAccountCommand command, MetaData metadata) {
        UserAccount userAccount = repository.load(command.getUserAccountId());
        userAccount.createBankAccount(command.getCurrency(), command.getAccountNumber(), metadata);
    }
    
    @CommandHandler
    public void handleSetUserAccountPassword(SetUserAccountPasswordCommand command, MetaData metadata) {
        UserAccount userAccount = repository.load(command.getUserAccountId());
        String passwordHash = BCrypt.hashpw(command.getPassword(), BCrypt.gensalt(5));
        userAccount.setPasswordHash(passwordHash, metadata);
    }

    @CommandHandler
    public void handleEnableUserAccount(EnableUserAccountCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).enable(metadata);
    }

    @CommandHandler
    public void handleDisableUserAccount(DisableUserAccountCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).disable(metadata);        
    }

    @CommandHandler
    public void handleAddRoles(AddUserAccountRolesCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).addRoles(command.getRoles(), metadata);        
    }

    @CommandHandler
    public void handleRemoveRoles(RemoveUserAccountRolesCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).removeRoles(command.getRoles(), metadata);        
    }

    @CommandHandler
    public void handleAddEmailContact(AddEmailContactCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).addEmailContact(command.getEmail(), metadata);
    }

    @CommandHandler
    public void handleAddPhoneNumberContact(AddPhoneNumberContactCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).addPhoneNumberContact(command.getPhoneNumber(), metadata);
    }

    @CommandHandler
    public void handleRequestValidationCode(RequestContactValidationCodeCommand command, MetaData metadata) {
        UserAccount userAccount = repository.load(command.getUserAccountId());
        Type contactType = userAccount.getContactDetail(command.getContactId()).getType();

        String validationCode;
        Date expiration;
        switch (contactType) {
            case EMAIL:
                validationCode = UUID.randomUUID().toString();
                expiration = Date.from(Instant.now().plus(Duration.ofDays(2)));
                break;
            case PHONE:
                validationCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
                expiration = Date.from(Instant.now().plus(Duration.ofMinutes(15)));
                break;
            default:
                throw new IllegalStateException("Unable to generate validation code for " + contactType);
        }
        userAccount.requestValidationCode(command.getContactId(), validationCode, expiration, metadata);
    }
    
    @CommandHandler
    public void handleValidateContact(ValidateContactDetailCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).validateContact(command.getContactId(), command.getValidatingCode(), metadata);        
    }

    @CommandHandler
    public void handleSetPaymentsCode(ChangeUserAccountPaymentsCode command, MetaData metadata) {
        repository.load(command.getUserAccountId()).changePaymentsCode(command.getCode(), metadata);        
    }

    @CommandHandler
    public void handleCreditOffer(CreditOfferFromUserAccountCommand command, MetaData metadata) {
        ExchangeOffer offer = offerAggregateRepository.load(command.getOfferId());
        CurrencyAmount amount = new CurrencyAmount(offer.getCurrencyOffered(), offer.getAmountOffered());
        repository.load(offer.getUserAccountId()).reserveMoneyForOffer(command.getTransactionId(), command.getOfferId(), amount, metadata);
    }

    @CommandHandler
    public void handleConfirmDebitReservation(ConfirmAccountDebitReservationCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).confirmDebitReservation(command.getTransactionId(), metadata);
    }

    @CommandHandler
    public void handleDiscardDebit(DiscardAccountDebitReservationCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).discardDebitReservation(command.getTransactionId(), metadata);
    }

    @CommandHandler
    public void handleCreditFromDeclinedOffer(CreditUserAccountFromDeclinedOfferCommand command, MetaData metadata) {
        ExchangeOffer offer = offerAggregateRepository.load(command.getOfferId());
        UserAccount userAccount = repository.load(command.getUserAccountId());
        userAccount.creditFromDeclinedOffer(command.getTransactionId(), command.getOfferId(), new CurrencyAmount(offer.getCurrencyOffered(), offer.getAmountOffered()), metadata);
    }
    
    @CommandHandler
    public void handleSendMoneyExternal(SendMoneyToUserBankAccountCommand command, MetaData metadata) {
        //TODO handle precision
        UserAccount userAccount = repository.load(command.getUserAccountId());
        userAccount.reserveMoneyForExternalBankAccount(command.getTransactionId(), command.getBankAccountId(), command.getAmount(), metadata);
    }
    
    @CommandHandler
    public void handleSendNotification(RequestSendNotificationToUserAccountCommand command, MetaData metadata) {
        repository.load(command.getUserAccountId()).requestNofitication(command.getNotificationTemplateId(), command.getTemplateData(), metadata);        
    }
    
}
