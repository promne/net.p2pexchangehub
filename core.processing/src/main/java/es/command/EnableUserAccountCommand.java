package es.command;

public class EnableUserAccountCommand {

    private final String userAccountId;

    public EnableUserAccountCommand(String userAccountId) {
        super();
        this.userAccountId = userAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }
    
}
