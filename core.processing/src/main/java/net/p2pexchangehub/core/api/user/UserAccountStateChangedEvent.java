package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.domain.UserAccountState;

public class UserAccountStateChangedEvent {

    private final String userAccountId;
    
    private final UserAccountState newState;

    public UserAccountStateChangedEvent(String userAccountId, UserAccountState newState) {
        super();
        this.userAccountId = userAccountId;
        this.newState = newState;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public UserAccountState getNewState() {
        return newState;
    }
    
}
