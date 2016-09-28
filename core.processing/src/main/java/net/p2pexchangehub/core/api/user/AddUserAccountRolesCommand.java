package net.p2pexchangehub.core.api.user;

import java.util.Set;

import net.p2pexchangehub.core.domain.UserAccountRole;

public class AddUserAccountRolesCommand {

    private final String userAccountId;
    
    private final Set<UserAccountRole> roles;

    public AddUserAccountRolesCommand(String userAccountId, Set<UserAccountRole> roles) {
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
