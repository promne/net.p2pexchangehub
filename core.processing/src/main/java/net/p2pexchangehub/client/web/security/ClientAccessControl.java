package net.p2pexchangehub.client.web.security;

import com.vaadin.cdi.access.JaasAccessControl;

import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

@Specializes
public class ClientAccessControl extends JaasAccessControl {

    @Inject
    private UserIdentity userIdentity;
    
    @Override
    public boolean isUserSignedIn() {
        return userIdentity.isLoggeedIn();
    }

    @Override
    public boolean isUserInRole(String role) {
        if (userIdentity.getUserAccount().isPresent()) {
            return userIdentity.getUserAccount().get().getRoles().stream().map(Object::toString).anyMatch(role::equalsIgnoreCase);
        }
        return false;
    }

    @Override
    public String getPrincipalName() {
        return userIdentity.getUserAccountId();
    }

}
