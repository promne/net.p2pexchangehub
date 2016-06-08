package net.p2pexchangehub.core.api.user;

public class UserAccountPasswordAuthenticationSucceededEvent {

    private final String userAccountId;

    public UserAccountPasswordAuthenticationSucceededEvent(String userAccountId) {
        super();
        this.userAccountId = userAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }
    
}
