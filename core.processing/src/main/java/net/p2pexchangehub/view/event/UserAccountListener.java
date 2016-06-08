package net.p2pexchangehub.view.event;

import javax.inject.Inject;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import george.test.exchange.core.domain.UserAccountState;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.user.UserAccountChargedFromOfferEvent;
import net.p2pexchangehub.core.api.user.UserAccountCreatedEvent;
import net.p2pexchangehub.core.api.user.UserAccountCreditedFromDeclinedOfferEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitDiscarderEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitForExternalBankAccountReservedEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitForOfferReservedEvent;
import net.p2pexchangehub.core.api.user.UserAccountNameChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPasswordChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPaymentsCodeChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesAddedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesRemovedEvent;
import net.p2pexchangehub.core.api.user.UserAccountStateChangedEvent;
import net.p2pexchangehub.core.api.user.UserIncomingTransactionMatchedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountCreatedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountOwnerNameChangedEvent;
import net.p2pexchangehub.core.api.user.contact.ContactDetailAddedEvent;
import net.p2pexchangehub.core.api.user.contact.ContactDetailValidatedEvent;
import net.p2pexchangehub.core.api.user.contact.EmailContactAddedEvent;
import net.p2pexchangehub.core.api.user.contact.PhoneNumberContactAddedEvent;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact;
import net.p2pexchangehub.view.domain.UserAccountContact.Type;
import net.p2pexchangehub.view.domain.UserAccountWallet;
import net.p2pexchangehub.view.domain.UserBankAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class UserAccountListener implements ReplayAware {

    @Inject
    private UserAccountRepository repository;
    
    @EventHandler
    public void accountCreated(UserAccountCreatedEvent event) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(event.getUserAccountId());
        userAccount.setUsername(event.getUsername());
        repository.save(userAccount);
    }
    
    @EventHandler
    public void handleUserIncomingTransactionMatched(UserIncomingTransactionMatchedEvent event) {
        updateWalletBalance(event.getUserAccountId(), event.getNewBalance());
    }
    
    @EventHandler
    public void handleUserBankAccountCreatedEvent(UserBankAccountCreatedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        UserBankAccount bankAccount = new UserBankAccount(event.getBankAccountId(), event.getCountry(), event.getCurrency(), event.getAccountNumber());
        userAccount.getBankAccounts().add(bankAccount);
        repository.save(userAccount);        
    }

    @EventHandler
    public void handleUserBankAccountCreatedEvent(UserBankAccountOwnerNameChangedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.getBankAccount(event.getBankAccountId()).get().setOwnerName(event.getOwnerName());
        repository.save(userAccount);        
    }
    
    @EventHandler
    public void handle(UserAccountPasswordChangedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.setPasswordHash(event.getNewPasswordHash());
        repository.save(userAccount);
    }

    @EventHandler
    public void handle(UserAccountNameChangedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.setName(event.getName());
        repository.save(userAccount);
    }

    @EventHandler
    public void handle(UserAccountStateChangedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.setEnabled(UserAccountState.ACTIVE==event.getNewState());
        repository.save(userAccount);
    }

    @EventHandler
    public void handle(UserAccountRolesAddedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.getRoles().addAll(event.getRoles());
        repository.save(userAccount);
    }

    @EventHandler
    public void handle(UserAccountRolesRemovedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.getRoles().removeAll(event.getRoles());
        repository.save(userAccount);
    }

    private void updateWalletBalance(String userAccountId, CurrencyAmount newBalance) {
        UserAccount userAccount = repository.findOne(userAccountId);
        UserAccountWallet wallet = new UserAccountWallet(newBalance);
        userAccount.getWallet().remove(wallet);
        userAccount.getWallet().add(wallet);
        repository.save(userAccount);        
    }
    
    @EventHandler
    public void handle(UserAccountDebitForOfferReservedEvent event) {
        updateWalletBalance(event.getUserAccountId(), event.getNewBalance());
    }

    @EventHandler
    public void handle(UserAccountCreditedFromDeclinedOfferEvent event) {
        updateWalletBalance(event.getUserAccountId(), event.getNewBalance());
    }

    @EventHandler
    public void handle(UserAccountChargedFromOfferEvent event) {
        updateWalletBalance(event.getUserAccountId(), event.getNewBalance());
    }

    @EventHandler
    public void handle(UserAccountDebitForExternalBankAccountReservedEvent event) {
        updateWalletBalance(event.getUserAccountId(), event.getNewBalance());
    }

    @EventHandler
    public void handle(UserAccountDebitDiscarderEvent event) {
        updateWalletBalance(event.getUserAccountId(), event.getNewBalance());
    }
    
    
    @EventHandler
    public void handle(ContactDetailAddedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        
        UserAccountContact newContact;
        if (PhoneNumberContactAddedEvent.class.equals(event.getClass())) {
            newContact = new UserAccountContact(((PhoneNumberContactAddedEvent)event).getNumber(), Type.PHONE);
        } else if (EmailContactAddedEvent.class.equals(event.getClass())) {
            newContact = new UserAccountContact(((EmailContactAddedEvent)event).getEmailAddress(), Type.EMAIL);
        } else {
            throw new IllegalStateException("Unable to handle " + event.getClass());
        }
        userAccount.getContacts().add(newContact);
        repository.save(userAccount);
    }

    @EventHandler
    public void handle(ContactDetailValidatedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.getContacts().stream().filter(uac -> uac.getValue().equals(event.getContactValue())).forEach(uac -> uac.setValidated(true));
        repository.save(userAccount);
    }

    @EventHandler
    public void handle(UserAccountPaymentsCodeChangedEvent event) {
        UserAccount userAccount = repository.findOne(event.getUserAccountId());
        userAccount.setPaymentsCode(event.getCode());
        repository.save(userAccount);
    }
    
    @Override
    public void beforeReplay() {
        repository.deleteAll();
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }    
    
}
