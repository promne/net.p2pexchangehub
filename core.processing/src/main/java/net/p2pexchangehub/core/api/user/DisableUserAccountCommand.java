package net.p2pexchangehub.core.api.user;

public class DisableUserAccountCommand {

    private final String userAccountId;

    public DisableUserAccountCommand(String userAccountId) {
        super();
        this.userAccountId = userAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }
    
}
