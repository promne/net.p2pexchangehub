package es.command;

public class AddPhoneNumberContactCommand {

    private final String userAccountId;
    
    private final String phoneNumber;

    public AddPhoneNumberContactCommand(String userAccountId, String phoneNumber) {
        super();
        this.userAccountId = userAccountId;
        this.phoneNumber = phoneNumber;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    
}
