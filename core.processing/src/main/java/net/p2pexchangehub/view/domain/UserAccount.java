package net.p2pexchangehub.view.domain;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;

import george.test.exchange.core.domain.UserAccountRole;
import net.p2pexchangehub.view.domain.UserAccountContact.Type;

public class UserAccount {

    @Id
    private String id;
    public static final String PROPERTY_ID = "id";
    
    private String username;
    public static final String PROPERTY_USERNAME = "username";

    private String name;
    public static final String PROPERTY_NAME = "name";

    private String passwordHash;
    
    private String paymentsCode;
    public static final String PROPERTY_PAYMENTS_CODE = "paymentsCode";
    
    private Set<UserAccountWallet> wallet = new HashSet<>();
    public static final String PROPERTY_WALLET = "wallet";

    private Set<UserBankAccount> bankAccounts = new HashSet<>();
    public static final String PROPERTY_BANK_ACCOUNTS = "bankAccounts";

    private Set<UserAccountContact> contacts = new HashSet<>();
    public static final String PROPERTY_CONTACTS = "contacts";
    
    private Set<UserAccountRole> roles = new HashSet<>();
    public static final String PROPERTY_ROLES = "roles";

    private boolean enabled;
    public static final String PROPERTY_ENABLED = "enabled";
    
    public UserAccount() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<UserAccountWallet> getWallet() {
        return wallet;
    }

    public void setWallet(Set<UserAccountWallet> wallet) {
        this.wallet = wallet;
    }

    public Set<UserBankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public Optional<UserBankAccount> getBankAccount(String currency, String accountNumber) {
        return bankAccounts.stream().filter(c -> c.getCurrency().equals(currency) && c.getAccountNumber().equals(accountNumber)).findAny();        
    }
    
    public void setBankAccounts(Set<UserBankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<UserAccountRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserAccountRole> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<UserAccountContact> getContacts() {
        return contacts;
    }

    public Set<UserAccountContact> getContacts(Type type) {
        return contacts.stream().filter(c -> c.getType() == type).collect(Collectors.toSet());
    }

    public Optional<UserAccountContact> getContact(String value) {
        return contacts.stream().filter(c -> c.getValue().equals(value)).findFirst();        
    }
    
    public Optional<UserAccountContact> getDefaultContact(Type type) {
        return contacts.stream().filter(c -> c.getType() == type).findFirst();
    }

    public void setContacts(Set<UserAccountContact> contacts) {
        this.contacts = contacts;
    }

    public String getPaymentsCode() {
        return paymentsCode;
    }

    public void setPaymentsCode(String paymentsCode) {
        this.paymentsCode = paymentsCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
