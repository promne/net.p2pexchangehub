package net.p2pexchangehub.client.web.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;

import george.test.exchange.core.domain.UserAccountRole;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@SessionScoped
public class UserIdentity implements Serializable {

    @Inject
    transient private UserAccountRepository userAccountRepository;
    
    private String userAccountId;
    
    public void setUserAccountId(String userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }
    
    public Optional<UserAccount> getUserAccount() {
        if (userAccountId!=null) {
            return Optional.of(userAccountRepository.findOne(userAccountId));            
        }
        return Optional.empty();
    }
    
    public boolean isLoggeedIn() {
        return getUserAccount().isPresent();
    }
    
    public void logout() {
        //TODO: call to aggregate
        userAccountId = null;
    }
    
    public boolean hasRole(UserAccountRole role) {
        Optional<UserAccount> userAccount = getUserAccount();
        return userAccount.isPresent() && userAccount.get().getRoles().contains(role);
    }

    public boolean hasAnyRole(UserAccountRole... roles) {
        Optional<UserAccount> userAccount = getUserAccount();
        return userAccount.isPresent() && CollectionUtils.containsAny(userAccount.get().getRoles(), Arrays.asList(roles));
    }

}
