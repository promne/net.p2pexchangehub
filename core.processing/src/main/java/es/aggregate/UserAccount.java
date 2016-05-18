package es.aggregate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import es.aggregate.value.ContactDetail;
import es.aggregate.value.ContactDetail.Type;
import es.event.ContactDetailRemovedEvent;
import es.event.ContactDetailValidatedEvent;
import es.event.ContactDetailValidationRequestedEvent;
import es.event.EmailContactAddedEvent;
import es.event.PhoneNumberContactAddedEvent;
import es.event.UserAccountCreatedEvent;
import es.event.UserAccountPasswordChangedEvent;
import es.event.UserAccountRolesAddedEvent;
import es.event.UserAccountRolesRemovedEvent;
import es.event.UserAccountStateChangedEvent;
import es.event.UserBankAccountCreatedEvent;
import es.event.UserIncomingTransactionMatchedEvent;
import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.domain.UserAccountState;

public class UserAccount extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;
    
    private Map<String, ContactDetail> contactDetails = new HashMap<>();

    private Map<String, UserBankAccount> bankAccounts = new HashMap<>();
    
    private Map<String, BigDecimal> currencyAccounts = new HashMap<>();
    
    private Set<UserAccountRole> roles = new HashSet<>();
    
    private UserAccountState state;
    
    public UserAccount() {
        super();
    }

    public UserAccount(String userAccountId, String username) {
        super();
        apply(new UserAccountCreatedEvent(userAccountId, username));
    }

    public Set<UserBankAccount> getBankAccounts() {
        return Collections.unmodifiableSet(new HashSet<>(bankAccounts.values()));
    }
    
    @EventHandler
    private void handle(UserAccountCreatedEvent event) {
        id = event.getUserAccountId();
//        username = event.getUsername();
    }

    public void createBankAccount(String currency, String accountNumber) {
        UserBankAccount userBankAccount = new UserBankAccount(UUID.randomUUID().toString(), currency, accountNumber);
        if (bankAccounts.values().contains(userBankAccount)) {
            throw new IllegalStateException(String.format("Bank account %s already exists for user account %s", userBankAccount, id));
        }
        apply(new UserBankAccountCreatedEvent(this.id, userBankAccount));
    }
    
    @EventHandler
    private void handle(UserBankAccountCreatedEvent event) {
        bankAccounts.put(event.getBankAccount().getId(), event.getBankAccount());
    }

    public void matchIncomingTransaction(String transactionId, BigDecimal amount, String currency) {
        BigDecimal currencyNewBalance = currencyAccounts.computeIfAbsent(currency, o -> BigDecimal.ZERO).add(amount);
        if (currencyNewBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(String.format("Unable to withdrawl %s %s for user %s from transaction %s", amount, currency, id, transactionId));            
        }
        apply(new UserIncomingTransactionMatchedEvent(id, transactionId, amount, currency, currencyNewBalance));
    }

    @EventHandler
    private void handle(UserIncomingTransactionMatchedEvent event) {
        currencyAccounts.put(event.getCurrency(), event.getNewBalance());
    }

    public void setPasswordHash(String newPasswordHash) {
        apply(new UserAccountPasswordChangedEvent(id, newPasswordHash));
    }

    @EventHandler
    private void handle(UserAccountPasswordChangedEvent event) {
        //nothing
    }

    public void enable() {
        if (this.state!=UserAccountState.ACTIVE) {
            apply(new UserAccountStateChangedEvent(id, UserAccountState.ACTIVE));
        }
    }

    public void disable() {
        if (this.state!=UserAccountState.DISABLED) {
            apply(new UserAccountStateChangedEvent(id, UserAccountState.DISABLED));
        }
    }
    
    @EventHandler
    private void handle(UserAccountStateChangedEvent event) {
        this.state = event.getNewState();
    }
    
    public void addRoles(Set<UserAccountRole> userAccountRoles) {
        apply(new UserAccountRolesAddedEvent(id, userAccountRoles));
    }
    
    @EventHandler
    private void handle(UserAccountRolesAddedEvent event) {
        roles.addAll(event.getRoles());
    }

    public void removeRoles(Set<UserAccountRole> userAccountRoles) {
        apply(new UserAccountRolesRemovedEvent(id, userAccountRoles));
    }
    
    @EventHandler
    private void handle(UserAccountRolesRemovedEvent event) {
        roles.removeAll(event.getRoles());
    }

    public void addEmailContact(String emailAddress) {
        if (contactDetails.values().stream().anyMatch(cd -> cd.getType()==Type.EMAIL)) {
            throw new IllegalStateException("User account " + id + " already has an email");
        }
        apply(new EmailContactAddedEvent(id, UUID.randomUUID().toString(), emailAddress));
    }
    
    @EventHandler
    private void handle(EmailContactAddedEvent event) {
        contactDetails.put(event.getContactDetailId(), new ContactDetail(event.getContactDetailId(), ContactDetail.Type.EMAIL, event.getEmailAddress()));
    }

    public void validateContact(String contactId, String validatingCode) {
        ContactDetail contactDetail = contactDetails.get(contactId);
        if (contactDetail!=null) {
            if (!contactDetail.isConfirmed()) {
                if (!contactDetail.getValidationCode().equals(validatingCode) || (new Date()).after(contactDetail.getValidationCodeExpiration())) {
                    throw new IllegalStateException("Wrong validating code or expired one for user account " + id + " contact detail " + contactId);
                }
                apply(new ContactDetailValidatedEvent(id, contactId));            
            }
        }
    }

    @EventHandler
    private void handle(ContactDetailValidatedEvent event) {
        contactDetails.get(event.getContactId()).setConfirmed(true);
    }
    
    public void removeContact(String contactId) {
        apply(new ContactDetailRemovedEvent(id, contactId));
    }

    @EventHandler
    private void handle(ContactDetailRemovedEvent event) {
        contactDetails.remove(event.getContactId());
    }
    
    public void addPhoneNumberContact(String phoneNumber) {
        if (contactDetails.values().stream().anyMatch(cd -> cd.getType()==Type.PHONE)) {
            throw new IllegalStateException("User account " + id + " already has a phone number");
        }
        apply(new PhoneNumberContactAddedEvent(id, UUID.randomUUID().toString(), phoneNumber));
    }

    @EventHandler
    private void handle(PhoneNumberContactAddedEvent event) {
        contactDetails.put(event.getContactDetailId(), new ContactDetail(event.getContactDetailId(), ContactDetail.Type.PHONE, event.getNumber()));        
    }

    public void requestValidationCode(String contactId, String validationCode, Date validationCodeExpiration) {
        if (!contactDetails.containsKey(contactId)) {
            throw new IllegalStateException("User account " + id + " doesn't have contact " + contactId);
        }
        apply(new ContactDetailValidationRequestedEvent(id, contactId, validationCode, validationCodeExpiration));
    }

    @EventHandler
    private void handle(ContactDetailValidationRequestedEvent event) {
        ContactDetail contactDetail = contactDetails.get(event.getContactId());
        contactDetail.setValidationCode(event.getValidationCode());
        contactDetail.setValidationCodeExpiration(event.getValidationCodeExpiration());
    }
    
}

