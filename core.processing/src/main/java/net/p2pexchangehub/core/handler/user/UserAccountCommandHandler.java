package net.p2pexchangehub.core.handler.user;

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
import net.p2pexchangehub.core.api.user.SendMoneyToUserBankAccountCommand;
import net.p2pexchangehub.core.api.user.SetUserAccountPasswordCommand;
import net.p2pexchangehub.core.api.user.bank.CreateUserBankAccountCommand;
import net.p2pexchangehub.core.api.user.contact.AddEmailContactCommand;
import net.p2pexchangehub.core.api.user.contact.AddPhoneNumberContactCommand;
import net.p2pexchangehub.core.api.user.contact.RequestContactValidationCodeCommand;
import net.p2pexchangehub.core.api.user.contact.ValidateContactDetailCommand;
import net.p2pexchangehub.core.handler.offer.ExchangeOffer;
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
    public void handleCreateUserAccount(CreateUserAccountCommand command) {
        Optional<net.p2pexchangehub.view.domain.UserAccount> existingUserAccount = userAccountRepository.findOneByUsername(command.getUsername());
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

    @CommandHandler
    public void handleSetPaymentsCode(ChangeUserAccountPaymentsCode command) {
        repository.load(command.getUserAccountId()).changePaymentsCode(command.getCode());        
    }

    @CommandHandler
    public void handleCreditOffer(CreditOfferFromUserAccountCommand command) {
        ExchangeOffer offer = offerAggregateRepository.load(command.getOfferId());
        CurrencyAmount amount = new CurrencyAmount(offer.getCurrencyOffered(), offer.getAmountOffered());
        repository.load(offer.getUserAccountId()).reserveMoneyForOffer(command.getTransactionId(), command.getOfferId(), amount);
    }

    @CommandHandler
    public void handleConfirmDebitReservation(ConfirmAccountDebitReservationCommand command) {
        repository.load(command.getUserAccountId()).confirmDebitReservation(command.getTransactionId());
    }

    @CommandHandler
    public void handleDiscardDebit(DiscardAccountDebitReservationCommand command) {
        repository.load(command.getUserAccountId()).discardDebitReservation(command.getTransactionId());
    }

    @CommandHandler
    public void handleCreditFromDeclinedOffer(CreditUserAccountFromDeclinedOfferCommand command) {
        ExchangeOffer offer = offerAggregateRepository.load(command.getOfferId());
        UserAccount userAccount = repository.load(command.getUserAccountId());
        userAccount.creditFromDeclinedOffer(command.getTransactionId(), command.getOfferId(), new CurrencyAmount(offer.getCurrencyOffered(), offer.getAmountOffered()));
    }
    
    @CommandHandler
    public void handleSendMoneyExternal(SendMoneyToUserBankAccountCommand command) {
        //TODO handle precision
        UserAccount userAccount = repository.load(command.getUserAccountId());
        userAccount.reserveMoneyForExternalBankAccount(command.getTransactionId(), command.getBankAccountId(), command.getAmount());
    }
    
 
    public void sendMoneyExternal() {
//        repository.load(command.getUserAccountId());
//        
//        Optional<BankProvider> bankProvider = getBankProvider(bankAccount.getBankType());
//
//        ExternalBankAccount bankAccountAggregate = repositoryAccounts.load(bankAccount.getId());
//        
//        TransactionRequestExternal transactionRequest = new TransactionRequestExternal();
//        transactionRequest.setBankAccount(bankAccountAggregate);
//        transactionRequest.setAmount(offer.getAmountRequested());
//        transactionRequest.setDetailInfo(String.format("%s %s %s rate %s", userAccount.getPaymentsCode(), offer.getAmountOffered(), offer.getCurrencyOffered(), offer.getAmountRequestedExchangeRate()));
//        transactionRequest.setRecipientAccountNumber(offer.getOwnerAccountNumber());
//        
//        bankProvider.get().processTransactionRequest(transactionRequest);
//        offer.requestPayment();
        
    }
    
}
