package net.p2pexchangehub.core.api.user;

public class UserAccountPasswordAuthenticationFailedEvent {

    private final String userAccountId;

    public UserAccountPasswordAuthenticationFailedEvent(String userAccountId) {
        super();
        this.userAccountId = userAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }
    
}
