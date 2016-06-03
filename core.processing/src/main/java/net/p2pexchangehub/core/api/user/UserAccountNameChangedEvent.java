package net.p2pexchangehub.core.api.user;

public class UserAccountNameChangedEvent {

    private final String userAccountId;
    
    private final String name;

    public UserAccountNameChangedEvent(String userAccountId, String name) {
        super();
        this.userAccountId = userAccountId;
        this.name = name;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getName() {
        return name;
    }
    
}
