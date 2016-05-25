package net.p2pexchangehub.core.api.user;

public class UserAccountPasswordChangedEvent {

    private final String userAccountId;
    
    private final String newPasswordHash;

    public UserAccountPasswordChangedEvent(String userAccountId, String newPasswordHash) {
        super();
        this.userAccountId = userAccountId;
        this.newPasswordHash = newPasswordHash;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getNewPasswordHash() {
        return newPasswordHash;
    }
    
}
