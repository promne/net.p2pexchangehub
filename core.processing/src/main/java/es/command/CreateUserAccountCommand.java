package es.command;

public class CreateUserAccountCommand {

    private final String userAccountId;
    
    private final String username;

    public CreateUserAccountCommand(String userAccountId, String username) {
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
