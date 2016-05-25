package net.p2pexchangehub.core.handler.user;

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

import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.domain.UserAccountState;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.user.MoneyForCashoutReservedEvent;
import net.p2pexchangehub.core.api.user.UserAccountChargedFromOfferEvent;
import net.p2pexchangehub.core.api.user.UserAccountCreatedEvent;
import net.p2pexchangehub.core.api.user.UserAccountCreditedFromDeclinedOfferEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitConfirmedEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitDiscarderEvent;
import net.p2pexchangehub.core.api.user.UserAccountDebitForOfferReservedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPasswordChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountPaymentsCodeChangedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesAddedEvent;
import net.p2pexchangehub.core.api.user.UserAccountRolesRemovedEvent;
import net.p2pexchangehub.core.api.user.UserAccountStateChangedEvent;
import net.p2pexchangehub.core.api.user.UserIncomingTransactionMatchedEvent;
import net.p2pexchangehub.core.api.user.bank.UserBankAccountCreatedEvent;
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

    public UserAccount(String userAccountId, String username) {
        super();
        apply(new UserAccountCreatedEvent(userAccountId, username));
    }

    private CurrencyAmount getCurrencyAccountBalance(CurrencyAmount currencyAmount) {
        return currencyAccounts.computeIfAbsent(currencyAmount.getCurrencyCode(), o -> new CurrencyAmount(o,BigDecimal.ZERO));
    }
    
    public Set<UserBankAccount> getBankAccounts() {
        return Collections.unmodifiableSet(new HashSet<>(bankAccounts.values()));
    }
    
    public String getPaymentsCode() {
        return paymentsCode;
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

    public void matchIncomingTransaction(String transactionId, CurrencyAmount amount) {
        if (!amount.isNotNegative()) {
            throw new IllegalStateException(String.format("Incoming transaction %s %s %s %s", amount.getAmount(), amount.getCurrencyCode(), id, transactionId));            
        }
        CurrencyAmount currencyNewBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserIncomingTransactionMatchedEvent(id, transactionId, amount, currencyNewBalance));
    }

    @EventHandler
    private void handle(UserIncomingTransactionMatchedEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }

    public void creditFromDeclinedOffer(String transactionId, String offerId, CurrencyAmount amount) {
        CurrencyAmount currencyNewBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserAccountCreditedFromDeclinedOfferEvent(id, transactionId, offerId, amount, currencyNewBalance));
    }

    @EventHandler
    private void handle(UserAccountCreditedFromDeclinedOfferEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }
    
    public void chargeFromOffer(String offerId, CurrencyAmount amount) {
        if (!amount.isNotNegative()) {
            throw new IllegalStateException(String.format("Charge from offer %s %s %s %s", amount.getAmount(), amount.getCurrencyCode(), id, offerId));            
        }
        CurrencyAmount currencyNewBalance = getCurrencyAccountBalance(amount).add(amount);
        apply(new UserAccountChargedFromOfferEvent(id, offerId, amount, currencyNewBalance));
    }
    
    @EventHandler
    private void handle(UserAccountChargedFromOfferEvent event) {
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
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

    public void changePaymentsCode(String code) {
        apply(new UserAccountPaymentsCodeChangedEvent(id, code));
    }

    @EventHandler
    private void handle(UserAccountPaymentsCodeChangedEvent event) {
        //nothing
        this.paymentsCode=event.getCode();
    }

    public void reserveMoneyForOffer(String transactionId, String offerId, CurrencyAmount amount) {
        CurrencyAmount newBalance = getCurrencyAccountBalance(amount).subtract(amount);
        if (newBalance.isNotNegative()) {
            apply(new UserAccountDebitForOfferReservedEvent(id, transactionId, offerId, amount, newBalance));
        }
    }

    @EventHandler
    private void handle(UserAccountDebitForOfferReservedEvent event) {
        currencyAccountsReservations.put(event.getTransactionId(), event.getAmount());
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }

    public void confirmDebitReservation(String transactionId) {
        CurrencyAmount amount = currencyAccountsReservations.get(transactionId);
        if (amount == null) {
            throw new IllegalStateException("Can't confirm debit of nonexisting transaction " + transactionId);
        }
        apply(new UserAccountDebitConfirmedEvent(id, transactionId, amount));
    }

    @EventHandler
    private void handle(UserAccountDebitConfirmedEvent event) {
        currencyAccountsReservations.remove(event.getTransactionId());
    }
    
    public void discardDebitReservation(String transactionId) {
        CurrencyAmount amount = currencyAccountsReservations.get(transactionId);
        if (amount == null) {
            throw new IllegalStateException("Can't discard debit of nonexisting transaction " + transactionId);
        }
        apply(new UserAccountDebitDiscarderEvent(id, transactionId, amount));
    }
    
    @EventHandler
    private void handle(UserAccountDebitDiscarderEvent event) {
        CurrencyAmount newAmount = getCurrencyAccountBalance(event.getAmount()).add(event.getAmount());
        currencyAccounts.put(newAmount.getCurrencyCode(), newAmount);
        currencyAccountsReservations.remove(event.getTransactionId());
    }
    
    
    public void reserveMoneyForCashout(String bankAccountId, CurrencyAmount amount) {
        CurrencyAmount newBalance = getCurrencyAccountBalance(amount).subtract(amount);
        if (newBalance.isNotNegative()) {
            apply(new MoneyForCashoutReservedEvent(id, bankAccountId, amount, newBalance));
        }
    }
    
    @EventHandler
    private void handle(MoneyForCashoutReservedEvent event) {
//        continue here, carry on with payment request on external gateway
        currencyAccounts.put(event.getNewBalance().getCurrencyCode(), event.getNewBalance());
    }


}

