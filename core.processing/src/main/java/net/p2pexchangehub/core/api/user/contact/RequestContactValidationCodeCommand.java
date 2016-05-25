package net.p2pexchangehub.core.api.user.contact;

public class RequestContactValidationCodeCommand {

    private final String userAccountId;

    private final String contactId;

    public RequestContactValidationCodeCommand(String userAccountId, String contactId) {
        super();
        this.userAccountId = userAccountId;
        this.contactId = contactId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getContactId() {
        return contactId;
    }

}
