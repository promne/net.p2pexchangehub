package net.p2pexchangehub.core.handler.user;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.mindrot.jbcrypt.BCrypt;

import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.domain.UserAccountState;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.user.UserAccountChargedFromOfferEvent;
import net.p2pexchangehub.core.api.user.UserAccountCreatedEvent;
import net.p2pexchangehub.core.api.user.UserAccountCreditedFromDeclinedOfferEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitConfirmedEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitDiscarderEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitForExternalBankAccountReservedEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitForOfferReservedEvent;
import net.p2pexchangehub.core.api.user.UserAccountNameChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountNotificationSendRequestedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPasswordAuthenticationFailedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPasswordAuthenticationSucceededEvent;
import net.p2pexchangehub.core.api.user.UserAccountPasswordChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPaymentsCodeChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesAddedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesRemovedEvent;
import net.p2pexchangehub.core.api.user.UserAccountStateChangedEvent;
import net.p2pexchangehub.core.api.user.UserIncomingTransactionMatchedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountCreatedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountOwnerNameChangedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountRemovedEvent;
import net.p2pexchangehub.core.api.user.contact.ContactDetailRemovedEvent;
import net.p2pexchangehub.core.api.user.contact.ContactDetailValidatedEvent;
import net.p2pexchangehub.core.api.user.contact.ContactDetailValidationRequestedEvent;
import net.p2pexchangehub.core.api.user.contact.EmailContactAddedEvent;
import net.p2pexchangehub.core.api.user.contact.PhoneNumberContactAddedEvent;
import net.p2pexchangehub.core.handler.user.ContactDetail.Type;

public class UserAccount extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;
    
    private Map<String, ContactDetail> contactDetails = new HashMap<>();

    /**
     * Key is currency + account number
     */
    private MultiKeyMap<String, UserBankAccount> bankAccounts = new MultiKeyMap<>();
    
    private Map<String, CurrencyAmount> currencyAccounts = new HashMap<>();

    private Map<String, CurrencyAmount> currencyAccountsReservations = new HashMap<>();
    
    private Set<UserAccountRole> roles = new HashSet<>();
    
    private UserAccountState state;

    private String paymentsCode;

    private String passwordHash;
    
    public UserAccount() {
        super();
    }

    public UserAccount(String userAccountId, String username, MetaData metadata) {
        super();
        apply(new UserAccountCreatedEvent(userAccountId, username), metadata);
    }

    private CurrencyAmount getCurrencyAccountBalance(CurrencyAmount currencyAmount) {
        return currencyAccounts.computeIfAbsent(currencyAmount.getCurrencyCode(), o -> new CurrencyAmount(o,BigDecimal.ZERO));
    }
    
    public ContactDetail getContactDetail(String contactValue) {
        return contactDetails.get(contactValue);
    }
    
    public String getPaymentsCode() {
        return paymentsCode;
    }

    @EventHandler
    private void handle(UserAccountCreatedEvent event) {
        id = event.getUserAccountId();
//        username = event.getUsername();
    }

    public void createBankAccount(String currency, String accountNumber, String ownerName, MetaData metadata) {
        if (!bankAccounts.containsKey(currency, accountNumber)) {
            apply(new UserBankAccountCreatedEvent(this.id, currency, accountNumber), metadata);
            apply(new UserBankAccountOwnerNameChangedEvent(this.id, currency, accountNumber, ownerName), metadata);            
        }
    }
    
    @EventHandler
    private void handle(UserBankAccountCreatedEvent event) {
        UserBankAccount userBankAccount = new UserBankAccount(event.getCurrency(), event.getAccountNumber());
        bankAccounts.put(event.getCurrency(), event.getAccountNumber(), userBankAccount);
    }

    @EventHandler
    private void handle(UserBankAccountOwnerNameChangedEvent event) {
        bankAccounts.get(event.getCurrency(), event.getAccountNumber()).setOwnerName(event.getOwnerName());
    }
    
    public void removeBankAccount(String currency, String accountNumber, MetaData metadata) {
        if (bankAccounts.containsKey(currency, accountNumber)) {
            apply(new UserBankAccountRemovedEvent(this.id, currency, accountNumber), metadata);            
        }
    }

    @EventHandler
    private void handle(UserBankAccountRemovedEvent event) {
        bankAccounts.removeMultiKey(event.getCurrency(), event.getAccountNumber());
    }

    public void matchIncomingTransaction(String transactionId, CurrencyAmount amount, MetaData metadata) {
        if (!amount.isNotNegative()) {
            throw new IllegalStateException(String.format("Incoming transaction %s %s %s %s", amount.getAmount(), amount.getCurrencyCode(), id, transactionId));            
        }
        CurrencyAmount currencyNewBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserIncomingTransactionMatchedEvent(id, transactionId, amount, currencyNewBalance), metadata);
    }

    @EventHandler
    private void handle(UserIncomingTransactionMatchedEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }

    public void creditFromDeclinedOffer(String transactionId, String offerId, CurrencyAmount amount, MetaData metadata) {
        CurrencyAmount currencyNewBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserAccountCreditedFromDeclinedOfferEvent(id, transactionId, offerId, amount, currencyNewBalance), metadata);
    }

    @EventHandler
    private void handle(UserAccountCreditedFromDeclinedOfferEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }
    
    public void chargeFromOffer(String offerId, CurrencyAmount amount, MetaData metadata) {
        if (!amount.isNotNegative()) {
            throw new IllegalStateException(String.format("Charge from offer %s %s %s %s", amount.getAmount(), amount.getCurrencyCode(), id, offerId));            
        }
        CurrencyAmount currencyNewBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserAccountChargedFromOfferEvent(id, offerId, amount, currencyNewBalance), metadata);
    }
    
    @EventHandler
    private void handle(UserAccountChargedFromOfferEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }

    public void setPasswordHash(String newPasswordHash, MetaData metadata) {
        apply(new UserAccountPasswordChangedEvent(id, newPasswordHash), metadata);
    }

    @EventHandler
    private void handle(UserAccountPasswordChangedEvent event) {
        passwordHash = event.getNewPasswordHash();
    }

    public void enable(MetaData metadata) {
        if (this.state!=UserAccountState.ACTIVE) {
            apply(new UserAccountStateChangedEvent(id, UserAccountState.ACTIVE), metadata);
        }
    }

    public void disable(MetaData metadata) {
        if (this.state!=UserAccountState.DISABLED) {
            apply(new UserAccountStateChangedEvent(id, UserAccountState.DISABLED), metadata);
        }
    }
    
    @EventHandler
    private void handle(UserAccountStateChangedEvent event) {
        this.state = event.getNewState();
    }
    
    public void addRoles(Set<UserAccountRole> userAccountRoles, MetaData metadata) {
        apply(new UserAccountRolesAddedEvent(id, userAccountRoles), metadata);
    }
    
    @EventHandler
    private void handle(UserAccountRolesAddedEvent event) {
        roles.addAll(event.getRoles());
    }

    public void removeRoles(Set<UserAccountRole> userAccountRoles, MetaData metadata) {
        apply(new UserAccountRolesRemovedEvent(id, userAccountRoles), metadata);
    }
    
    @EventHandler
    private void handle(UserAccountRolesRemovedEvent event) {
        roles.removeAll(event.getRoles());
    }

    public void addEmailContact(String emailAddress, MetaData metadata) {
        if (contactDetails.values().stream().anyMatch(cd -> cd.getType()==Type.EMAIL)) {
            throw new IllegalStateException("User account " + id + " already has an email");
        }
        apply(new EmailContactAddedEvent(id, emailAddress), metadata);
    }
    
    @EventHandler
    private void handle(EmailContactAddedEvent event) {
        contactDetails.put(event.getEmailAddress(), new ContactDetail(ContactDetail.Type.EMAIL, event.getEmailAddress()));
    }

    public void validateContact(String validatingCode, MetaData metadata) {
        List<ContactDetail> matchingContacts = contactDetails.values().stream()
            .filter(c -> !c.isConfirmed())
            .filter(c-> c.getValidationCodeExpiration().after(new Date()))
            .filter(c -> c.getValidationCode().equals(validatingCode))
            .collect(Collectors.toList());
        if (matchingContacts.size()>1) {
            throw new IllegalStateException("Unable to handle multiple matching contacts for code " + validatingCode);            
        }
        if (!matchingContacts.isEmpty()) {
            ContactDetail cd = matchingContacts.get(0);
            apply(new ContactDetailValidatedEvent(id, cd.getValue()), metadata);            
        }
    }

    @EventHandler
    private void handle(ContactDetailValidatedEvent event) {
        contactDetails.get(event.getContactValue()).setConfirmed(true);
    }
    
    public void removeContact(String contactValue, MetaData metadata) {
        apply(new ContactDetailRemovedEvent(id, contactValue), metadata);
    }

    @EventHandler
    private void handle(ContactDetailRemovedEvent event) {
        contactDetails.remove(event.getContactValue());
    }
    
    public void addPhoneNumberContact(String phoneNumber, MetaData metadata) {
        if (contactDetails.values().stream().anyMatch(cd -> cd.getType()==Type.PHONE)) {
            throw new IllegalStateException("User account " + id + " already has a phone number");
        }
        apply(new PhoneNumberContactAddedEvent(id, phoneNumber), metadata);
    }

    @EventHandler
    private void handle(PhoneNumberContactAddedEvent event) {
        contactDetails.put(event.getNumber(), new ContactDetail(ContactDetail.Type.PHONE, event.getNumber()));        
    }

    public void requestValidationCode(String contactValue, String validationCode, Date validationCodeExpiration, MetaData metadata) {
        if (!contactDetails.containsKey(contactValue)) {
            throw new IllegalStateException("User account " + id + " doesn't have contact " + contactValue);
        }
        apply(new ContactDetailValidationRequestedEvent(id, contactValue, validationCode, validationCodeExpiration), metadata);
    }

    @EventHandler
    private void handle(ContactDetailValidationRequestedEvent event) {
        ContactDetail contactDetail = contactDetails.get(event.getContactValue());
        contactDetail.setValidationCode(event.getValidationCode());
        contactDetail.setValidationCodeExpiration(event.getValidationCodeExpiration());
    }

    public void changePaymentsCode(String code, MetaData metadata) {
        apply(new UserAccountPaymentsCodeChangedEvent(id, code), metadata);
    }

    @EventHandler
    private void handle(UserAccountPaymentsCodeChangedEvent event) {
        //nothing
        this.paymentsCode=event.getCode();
    }

    public void reserveMoneyForOffer(String transactionId, String offerId, CurrencyAmount amount, MetaData metadata) {
        CurrencyAmount newBalance = getCurrencyAccountBalance(amount).subtract(amount);
        if (newBalance.isNotNegative()) {
            apply(new UserAccountDebitForOfferReservedEvent(id, transactionId, offerId, amount, newBalance), metadata);
        }
    }

    @EventHandler
    private void handle(UserAccountDebitForOfferReservedEvent event) {
        currencyAccountsReservations.put(event.getTransactionId(), event.getAmount());
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }

    public void confirmDebitReservation(String transactionId, MetaData metadata) {
        CurrencyAmount amount = currencyAccountsReservations.get(transactionId);
        if (amount == null) {
            throw new IllegalStateException("Can't confirm debit of nonexisting transaction " + transactionId);
        }
        apply(new UserAccountDebitConfirmedEvent(id, transactionId, amount), metadata);
    }

    @EventHandler
    private void handle(UserAccountDebitConfirmedEvent event) {
        currencyAccountsReservations.remove(event.getTransactionId());
    }
    
    public void discardDebitReservation(String transactionId, MetaData metadata) {
        CurrencyAmount amount = currencyAccountsReservations.get(transactionId);
        if (amount == null) {
            throw new IllegalStateException("Can't discard debit of nonexisting transaction " + transactionId);
        }
        CurrencyAmount newBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserAccountDebitDiscarderEvent(id, transactionId, amount, newBalance), metadata);
    }
    
    @EventHandler
    private void handle(UserAccountDebitDiscarderEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
        currencyAccountsReservations.remove(event.getTransactionId());
    }

    public void reserveMoneyForExternalBankAccount(String transactionId, String bankAccountNumber, CurrencyAmount amount, MetaData metadata) {
        CurrencyAmount newBalance = getCurrencyAccountBalance(amount).subtract(amount);
        if (newBalance.isNotNegative()) {
            apply(new UserAccountDebitForExternalBankAccountReservedEvent(id, transactionId, bankAccountNumber, amount, newBalance), metadata);
        }
    }

    @EventHandler
    private void handle(UserAccountDebitForExternalBankAccountReservedEvent event) {
        currencyAccountsReservations.put(event.getTransactionId(), event.getAmount());
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }

    public void requestNofitication(String notificationTemplateId, Map<String, Object> templateData, MetaData metaData) {
        apply(new UserAccountNotificationSendRequestedEvent(id, notificationTemplateId, templateData), metaData);
    }

    @EventHandler
    private void handle(UserAccountNotificationSendRequestedEvent event) {
        // nothing
    }

    public void setName(String name, MetaData metadata) {
        apply(new UserAccountNameChangedEvent(id, name), metadata);
    }
    
    @EventHandler
    private void handle(UserAccountNameChangedEvent event) {
        // nothing
    }

    public Boolean authenticate(String password, MetaData metadata) {
        if (BCrypt.checkpw(password, passwordHash)) {
            apply(new UserAccountPasswordAuthenticationSucceededEvent(id), metadata);
            return Boolean.TRUE; 
        } else {
            apply(new UserAccountPasswordAuthenticationFailedEvent(id), metadata);            
            return Boolean.FALSE; 
        }        
    }

    @EventHandler
    private void handle(UserAccountPasswordAuthenticationSucceededEvent event) {
        // nothing
    }
    
    @EventHandler
    private void handle(UserAccountPasswordAuthenticationFailedEvent event) {
        // nothing
    }
    
}

