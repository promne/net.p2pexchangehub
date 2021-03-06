package net.p2pexchangehub.client.rest.security;

import javax.enterprise.context.RequestScoped;

import net.p2pexchangehub.view.domain.UserAccount;

@RequestScoped
public class UserIdentityRest {

    private UserAccount userAccount;

    public UserIdentityRest() {
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
