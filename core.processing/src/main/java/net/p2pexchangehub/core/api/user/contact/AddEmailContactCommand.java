package net.p2pexchangehub.core.api.user.contact;

public class AddEmailContactCommand {

    private final String userAccountId;
    
    private final String email;

    public AddEmailContactCommand(String userAccountId, String email) {
        super();
        this.userAccountId = userAccountId;
        this.email = email;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getEmail() {
        return email;
    }
    
}
