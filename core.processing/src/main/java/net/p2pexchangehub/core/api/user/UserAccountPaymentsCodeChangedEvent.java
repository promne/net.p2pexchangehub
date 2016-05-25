package net.p2pexchangehub.core.api.user;

public class UserAccountPaymentsCodeChangedEvent {

    private final String userAccountId;
    
    private final String code;

    public UserAccountPaymentsCodeChangedEvent(String userAccountId, String code) {
        super();
        this.userAccountId = userAccountId;
        this.code = code;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getCode() {
        return code;
    }
    
}
