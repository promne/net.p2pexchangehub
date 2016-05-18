package george.test.exchange.rest.client.security;

import javax.enterprise.context.RequestScoped;

import esw.domain.UserAccount;

@RequestScoped
public class UserIdentity {

    private UserAccount userAccount;

    public UserIdentity() {
        super();
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public boolean hasUserAccount() {
        return userAccount != null;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
        
}
