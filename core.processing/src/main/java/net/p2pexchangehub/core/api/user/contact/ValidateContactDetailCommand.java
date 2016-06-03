package net.p2pexchangehub.core.api.user.contact;

public class ValidateContactDetailCommand {

    private final String userAccountId;

    private final String validatingCode;

    public ValidateContactDetailCommand(String userAccountId, String validatingCode) {
        super();
        this.userAccountId = userAccountId;
        this.validatingCode = validatingCode;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getValidatingCode() {
        return validatingCode;
    }

}
