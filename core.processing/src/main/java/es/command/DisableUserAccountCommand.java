package es.command;

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
