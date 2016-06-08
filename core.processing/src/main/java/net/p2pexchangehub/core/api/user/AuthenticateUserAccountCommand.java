package net.p2pexchangehub.core.api.user;

public class AuthenticateUserAccountCommand {

    private final String username;
    
    private final String password;

    public AuthenticateUserAccountCommand(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
        
}
