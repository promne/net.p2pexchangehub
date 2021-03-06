package net.p2pexchangehub.core.api.user;

import java.util.Set;

import net.p2pexchangehub.core.domain.UserAccountRole;

public class RemoveUserAccountRolesCommand {

    private final String userAccountId;
    
    private final Set<UserAccountRole> roles;

    public RemoveUserAccountRolesCommand(String userAccountId, Set<UserAccountRole> roles) {
        super();
        this.userAccountId = userAccountId;
        this.roles = roles;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public Set<UserAccountRole> getRoles() {
        return roles;
    }
    
}
