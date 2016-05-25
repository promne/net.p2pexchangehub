package net.p2pexchangehub.core.api.user.contact;

public class ValidateContactDetailCommand {

    private final String userAccountId;

    private final String contactId;

    private final String validatingCode;

    public ValidateContactDetailCommand(String userAccountId, String contactId, String validatingCode) {
        super();
        this.userAccountId = userAccountId;
        this.contactId = contactId;
        this.validatingCode = validatingCode;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getContactId() {
        return contactId;
    }

    public String getValidatingCode() {
        return validatingCode;
    }

}
