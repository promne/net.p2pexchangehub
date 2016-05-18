package esw.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKeyJoinColumn;

import george.test.exchange.core.domain.UserAccountRole;

@Entity
public class UserAccount {

    @Id
    private String id;
    public static final String PROPERTY_ID = "id";
    
    private String username;
    public static final String PROPERTY_USERNAME = "username";

    private String passwordHash;
    
    @ElementCollection
    private Set<UserAccountWallet> wallet = new HashSet<>();
    public static final String PROPERTY_WALLET = "wallet";

    @ElementCollection
    private Set<UserBankAccount> bankAccounts = new HashSet<>();
    public static final String PROPERTY_BANK_ACCOUNTS = "bankAccounts";

    @ElementCollection
    @MapKeyJoinColumn(referencedColumnName = UserAccountContact.PROPERTY_ID)
    private Map<String, UserAccountContact> contacts = new HashMap<>();
    public static final String PROPERTY_CONTACTS = "contacts";
    
    @ElementCollection(fetch=FetchType.EAGER)
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

    public Map<String, UserAccountContact> getContacts() {
        return contacts;
    }

    public void setContacts(Map<String, UserAccountContact> contacts) {
        this.contacts = contacts;
    }

}
