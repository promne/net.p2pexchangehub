package es.aggregate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import es.event.UserAccountCreatedEvent;
import es.event.UserBankAccountCreatedEvent;
import es.event.UserIncomingTransactionMatchedEvent;

public class UserAccount extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;
    
    private String username;

    private Map<String, UserBankAccount> bankAccounts = new HashMap<>();
    
    private Map<String, BigDecimal> currencyAccounts = new HashMap<>();
    
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
        username = event.getUsername();
    }

    public void createBankAccount(String currency, String country, String accountNumber) {
        UserBankAccount userBankAccount = new UserBankAccount(UUID.randomUUID().toString(), currency, country, accountNumber);
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
    
}
