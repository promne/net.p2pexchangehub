package esw.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserAccount {

    @Id
    private String id;
    
    private String username;

    @ElementCollection
    private Set<UserAccountWallet> wallet = new HashSet<>();
    
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

}
