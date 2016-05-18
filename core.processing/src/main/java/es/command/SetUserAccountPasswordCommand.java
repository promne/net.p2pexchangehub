package es.command;

public class SetUserAccountPasswordCommand {

    private final String userAccountId;
    
    private final String password;

    public SetUserAccountPasswordCommand(String userAccountId, String password) {
        super();
        this.userAccountId = userAccountId;
        this.password = password;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getPassword() {
        return password;
    }
    
}
