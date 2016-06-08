package net.p2pexchangehub.core.api.user.contact;

public class RequestContactValidationCodeCommand {

    private final String userAccountId;

    private final String contactValue;

    public RequestContactValidationCodeCommand(String userAccountId, String contactValue) {
        super();
        this.userAccountId = userAccountId;
        this.contactValue = contactValue;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getContactValue() {
        return contactValue;
    }

}
