package es.event;

public class UserAccountCreatedEvent {

    private final String userAccountId;
    
    private final String username;

    public UserAccountCreatedEvent(String userAccountId, String username) {
        super();
        this.userAccountId = userAccountId;
        this.username = username;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getUsername() {
        return username;
    }

}
