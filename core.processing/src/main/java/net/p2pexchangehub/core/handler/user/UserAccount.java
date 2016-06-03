package net.p2pexchangehub.core.handler.user;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

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
import net.p2pexchangehub.core.api.user.UserAccountPasswordChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPaymentsCodeChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesAddedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesRemovedEvent;
import net.p2pexchangehub.core.api.user.UserAccountStateChangedEvent;
import net.p2pexchangehub.core.api.user.UserIncomingTransactionMatchedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountCreatedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountOwnerNameChangedEvent;
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

    private Map<String, UserBankAccount> bankAccounts = new HashMap<>();
    
    private Map<String, CurrencyAmount> currencyAccounts = new HashMap<>();

    private Map<String, CurrencyAmount> currencyAccountsReservations = new HashMap<>();
    
    private Set<UserAccountRole> roles = new HashSet<>();
    
    private UserAccountState state;

    private String paymentsCode;
    
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
    
    public Set<UserBankAccount> getBankAccounts() {
        return Collections.unmodifiableSet(new HashSet<>(bankAccounts.values()));
    }

    public Optional<UserBankAccount> getBankAccount(String id) {
        return Optional.ofNullable(bankAccounts.get(id));
    }
    
    public ContactDetail getContactDetail(String contactId) {
        return contactDetails.get(contactId);
    }
    
    public String getPaymentsCode() {
        return paymentsCode;
    }

    @EventHandler
    private void handle(UserAccountCreatedEvent event) {
        id = event.getUserAccountId();
//        username = event.getUsername();
    }

    public void createBankAccount(String country, String currency, String accountNumber, String ownerName, MetaData metadata) {
        String bankAccountId = UUID.randomUUID().toString();
        apply(new UserBankAccountCreatedEvent(this.id, bankAccountId, country, currency, accountNumber), metadata);
        apply(new UserBankAccountOwnerNameChangedEvent(this.id, bankAccountId, ownerName), metadata);
    }
    
    @EventHandler
    private void handle(UserBankAccountCreatedEvent event) {
        UserBankAccount userBankAccount = new UserBankAccount(event.getBankAccountId(), event.getCountry(), event.getCurrency(), event.getAccountNumber());
        bankAccounts.put(event.getBankAccountId(), userBankAccount);
    }

    @EventHandler
    private void handle(UserBankAccountOwnerNameChangedEvent event) {
        bankAccounts.get(event.getBankAccountId()).setOwnerName(event.getOwnerName());
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
        //nothing
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
        apply(new EmailContactAddedEvent(id, UUID.randomUUID().toString(), emailAddress), metadata);
    }
    
    @EventHandler
    private void handle(EmailContactAddedEvent event) {
        contactDetails.put(event.getContactDetailId(), new ContactDetail(event.getContactDetailId(), ContactDetail.Type.EMAIL, event.getEmailAddress()));
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
            apply(new ContactDetailValidatedEvent(id, cd.getId()), metadata);            
        }
    }

    @EventHandler
    private void handle(ContactDetailValidatedEvent event) {
        contactDetails.get(event.getContactId()).setConfirmed(true);
    }
    
    public void removeContact(String contactId, MetaData metadata) {
        apply(new ContactDetailRemovedEvent(id, contactId), metadata);
    }

    @EventHandler
    private void handle(ContactDetailRemovedEvent event) {
        contactDetails.remove(event.getContactId());
    }
    
    public void addPhoneNumberContact(String phoneNumber, MetaData metadata) {
        if (contactDetails.values().stream().anyMatch(cd -> cd.getType()==Type.PHONE)) {
            throw new IllegalStateException("User account " + id + " already has a phone number");
        }
        apply(new PhoneNumberContactAddedEvent(id, UUID.randomUUID().toString(), phoneNumber), metadata);
    }

    @EventHandler
    private void handle(PhoneNumberContactAddedEvent event) {
        contactDetails.put(event.getContactDetailId(), new ContactDetail(event.getContactDetailId(), ContactDetail.Type.PHONE, event.getNumber()));        
    }

    public void requestValidationCode(String contactId, String validationCode, Date validationCodeExpiration, MetaData metadata) {
        if (!contactDetails.containsKey(contactId)) {
            throw new IllegalStateException("User account " + id + " doesn't have contact " + contactId);
        }
        apply(new ContactDetailValidationRequestedEvent(id, contactId, validationCode, validationCodeExpiration), metadata);
    }

    @EventHandler
    private void handle(ContactDetailValidationRequestedEvent event) {
        ContactDetail contactDetail = contactDetails.get(event.getContactId());
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

    public void reserveMoneyForExternalBankAccount(String transactionId, String bankAccountId, CurrencyAmount amount, MetaData metadata) {
        CurrencyAmount newBalance = getCurrencyAccountBalance(amount).subtract(amount);
        if (newBalance.isNotNegative() && bankAccounts.containsKey(bankAccountId)) {
            apply(new UserAccountDebitForExternalBankAccountReservedEvent(id, transactionId, bankAccountId, amount, newBalance), metadata);
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
    
}

