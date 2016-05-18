package es.aggregate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import es.event.ExternalBankAccountActiveSetEvent;
import es.event.ExternalBankAccountCommunicationLoggedEvent;
import es.event.ExternalBankAccountCreatedEvent;
import es.event.ExternalBankAccountCredentialsSetEvent;
import es.event.ExternalBankAccountSynchronizationRequestedEvent;
import es.event.ExternalBankAccountSynchronizedEvent;
import george.test.exchange.core.domain.ExternalBankType;

public abstract class ExternalBankAccount extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String currency;

    private BigDecimal balance;

    private Date lastCheck;

    private boolean active;

    private String accountNumber;

    private String username;

    private String password;

    private ExternalBankType bankType;
    
    private Properties providerConfiguration;

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


    public ExternalBankAccount(String id, String currency, String accountNumber, ExternalBankType bankType) {
        super();
        apply(new ExternalBankAccountCreatedEvent(id, currency, accountNumber, bankType));
    }
    
    @EventHandler
    private void handleCreated(ExternalBankAccountCreatedEvent event) {
        accountNumber = event.getAccountNumber();
        id = event.getBankAccountId();
        bankType = event.getBankType();
        currency = event.getCurrency();
    }
    
    public void setCredentials(String username, String password) {
        apply(new ExternalBankAccountCredentialsSetEvent(id, username, password));
    }
    
    @EventHandler
    private void handleCredentialsSet(ExternalBankAccountCredentialsSetEvent event) {
        username = event.getUsername();
        password = event.getPassword();
    }
 
    public void requestSynchronization() {
        apply(new ExternalBankAccountSynchronizationRequestedEvent(id));        
    }

    @EventHandler
    private void handleSynchronizationRequested(ExternalBankAccountSynchronizationRequestedEvent event) {
        // nothing
    }
    
    public void setSynchronized(Date syncDate, BigDecimal balance) {
        apply(new ExternalBankAccountSynchronizedEvent(id, syncDate, balance));
    }

    @EventHandler
    private void handleSynchronized(ExternalBankAccountSynchronizedEvent event) {
        lastCheck = event.getSyncDate();
        balance = event.getBalance();
    }

    public void setActive(boolean active) {
        apply(new ExternalBankAccountActiveSetEvent(id, active));
    }

    @EventHandler
    private void handleActiveSet(ExternalBankAccountActiveSetEvent event) {
        active = event.isActive();        
    }

    public void logCommunication(String data) {
        apply(new ExternalBankAccountCommunicationLoggedEvent(id, data));
    }
    
    @EventHandler
    private void handleLogCommunication(ExternalBankAccountCommunicationLoggedEvent event) {
        //do nothing
    }
    
}
