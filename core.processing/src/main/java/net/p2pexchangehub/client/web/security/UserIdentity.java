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
    
    transient private Optional<UserAccount> userAccount = Optional.empty();
        
    public void setUserAccountId(String userAccountId) {
        this.userAccountId = userAccountId;
        userAccount = Optional.empty();
    }

    public String getUserAccountId() {
        return userAccountId;
    }
    
    public Optional<UserAccount> getUserAccount() {
        if (!userAccount.isPresent() && userAccountId!=null) {
            userAccount = Optional.of(userAccountRepository.findOne(userAccountId));            
        }
        return userAccount;
    }
    
    public boolean isLoggeedIn() {
        return getUserAccount().isPresent();
    }
    
    public void logout() {
        //TODO: call to aggregate
        userAccountId = null;
        userAccount = Optional.empty();
    }
    
    public boolean hasRole(UserAccountRole role) {
        return getUserAccount().isPresent() && getUserAccount().get().getRoles().contains(role);
    }

    public boolean hasAnyRole(UserAccountRole... roles) {
        return getUserAccount().isPresent() && CollectionUtils.containsAny(getUserAccount().get().getRoles(), Arrays.asList(roles));
    }

}
