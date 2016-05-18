package es.command;

import java.util.Set;

import george.test.exchange.core.domain.UserAccountRole;

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
