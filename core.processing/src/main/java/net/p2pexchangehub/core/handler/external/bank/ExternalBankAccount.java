package net.p2pexchangehub.core.handler.external.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.annotation.Timestamp;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.joda.time.DateTime;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountActiveSetEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCommunicationLoggedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCredentialsSetEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountSynchronizationEnabledSetEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountSynchronizationRequestedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountSynchronizedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankTransactionRequestConfirmedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankTransactionRequestFailedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankTransactionRequestSucceededEvent;
import net.p2pexchangehub.core.domain.ExternalBankType;

public abstract class ExternalBankAccount extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String currency;

    private BigDecimal balance;

    private Date lastCheck;

    private boolean active; //TODO: do checks for this attribute

    private boolean synchronizationEnabled;

    private String accountNumber;

    private String username;

    private String password;

    private ExternalBankType bankType;
    
    private Properties providerConfiguration;
    
    private Map<String, ExternalTransactionRequest> pendingRequests = new HashMap<>();

    public ExternalBankAccount() {
        super();
    }
    
    public String getId() {
        return id;
    }

    public Date getLastCheck() {
        return lastCheck;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSynchronizationEnabled() {
        return synchronizationEnabled;
    }

    public ExternalBankType getBankType() {
        return bankType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Map<String, ExternalTransactionRequest> getPendingRequests() {
        return new HashMap<>(pendingRequests);
    }

    public ExternalBankAccount(String id, String currency, String accountNumber, ExternalBankType bankType, MetaData metadata) {
        super();
        apply(new ExternalBankAccountCreatedEvent(id, currency, accountNumber, bankType), metadata);
    }
    
    @EventHandler
    private void handleCreated(ExternalBankAccountCreatedEvent event) {
        accountNumber = event.getAccountNumber();
        id = event.getBankAccountId();
        bankType = event.getBankType();
        currency = event.getCurrency();
    }
    
    public void setCredentials(String username, String password, MetaData metadata) {
        apply(new ExternalBankAccountCredentialsSetEvent(id, username, password), metadata);
    }
    
    @EventHandler
    private void handleCredentialsSet(ExternalBankAccountCredentialsSetEvent event) {
        username = event.getUsername();
        password = event.getPassword();
    }
 
    public void requestSynchronization(MetaData metadata) {
        apply(new ExternalBankAccountSynchronizationRequestedEvent(id), metadata);        
    }

    @EventHandler
    private void handleSynchronizationRequested(ExternalBankAccountSynchronizationRequestedEvent event) {
        // nothing
    }
    
    public void setSynchronized(Date syncDate, BigDecimal balance, MetaData metadata) {
        apply(new ExternalBankAccountSynchronizedEvent(id, syncDate, balance), metadata);
    }

    @EventHandler
    private void handleSynchronized(ExternalBankAccountSynchronizedEvent event) {
        lastCheck = event.getSyncDate();
        balance = event.getBalance();
    }

    public void setActive(boolean active, MetaData metadata) {
        apply(new ExternalBankAccountActiveSetEvent(id, active), metadata);
    }

    @EventHandler
    private void handleActiveSet(ExternalBankAccountActiveSetEvent event) {
        active = event.isActive();        
    }

    public void setSynchronizationEnabled(boolean active, MetaData metadata) {
        apply(new ExternalBankAccountSynchronizationEnabledSetEvent(id, active), metadata);
    }
    
    @EventHandler
    private void handleSynchronizationEnabledSet(ExternalBankAccountSynchronizationEnabledSetEvent event) {
        synchronizationEnabled = event.isEnabled();        
    }

    public void logCommunication(String data, MetaData metadata) {
        apply(new ExternalBankAccountCommunicationLoggedEvent(id, data), metadata);
    }
    
    @EventHandler
    private void handleLogCommunication(ExternalBankAccountCommunicationLoggedEvent event) {
        //do nothing
    }

    public void externalTransactionRequestSucceeded(String transactionId, String userAccountId, String userBankAccountId, CurrencyAmount amount, MetaData metadata) {
        apply(new ExternalBankTransactionRequestSucceededEvent(id, transactionId, userAccountId, userBankAccountId, amount), metadata);
    }

    @EventHandler
    private void handleExternalTransactionRequestSucceeded(ExternalBankTransactionRequestSucceededEvent event, @Timestamp DateTime jodaTimestamp) {
        ExternalTransactionRequest request = new ExternalTransactionRequest(event.getTransactionId(), event.getUserAccountId(), event.getUserBankAccountId(), event.getAmount(), jodaTimestamp.toDate());
        pendingRequests.put(request.getTransactionId(), request);
    }

    public void externalTransactionRequestFailed(String transactionId, String userAccountId, String userBankAccountId, CurrencyAmount amount, MetaData metadata) {
        apply(new ExternalBankTransactionRequestFailedEvent(id, transactionId, userAccountId, userBankAccountId, amount), metadata);
    }

    @EventHandler
    private void handleExternalTransactionRequestFailed(ExternalBankTransactionRequestFailedEvent event) {
        //nothing to do
    }

    public void externalTransactionRequestConfirmed(String pendingTransactionId, String externalBankTransactionId, MetaData metadata) {
        ExternalTransactionRequest pendingTransaction = pendingRequests.get(pendingTransactionId);
        if (pendingTransaction != null) {
            apply(new ExternalBankTransactionRequestConfirmedEvent(id, pendingTransactionId, pendingTransaction.getUserAccountId(), pendingTransaction.getUserBankAccountId(), pendingTransaction.getAmount(), externalBankTransactionId), metadata);            
        }
    }
    
    @EventHandler
    private void handleExternalTransactionRequestConfirmed(ExternalBankTransactionRequestConfirmedEvent event) {
        pendingRequests.remove(event.getTransactionId());
    }
    
}
