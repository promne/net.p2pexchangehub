package net.p2pexchangehub.core.api.user.contact;

import java.util.Date;

public class ContactDetailValidationRequestedEvent {

    private final String userAccountId;

    private final String contactValue;

    private final String validationCode;

    private final Date validationCodeExpiration;

    public ContactDetailValidationRequestedEvent(String userAccountId, String contactValue, String validationCode, Date validationCodeExpiration) {
        super();
        this.userAccountId = userAccountId;
        this.contactValue = contactValue;
        this.validationCode = validationCode;
        this.validationCodeExpiration = validationCodeExpiration;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getContactValue() {
        return contactValue;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public Date getValidationCodeExpiration() {
        return validationCodeExpiration;
    }
    
}
