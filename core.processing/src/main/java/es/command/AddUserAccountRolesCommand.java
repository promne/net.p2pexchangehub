package es.command;

import java.util.Set;

import george.test.exchange.core.domain.UserAccountRole;

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
