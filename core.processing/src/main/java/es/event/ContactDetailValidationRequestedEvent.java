package es.event;

import java.util.Date;

public class ContactDetailValidationRequestedEvent {

    private final String userAccountId;

    private final String contactId;

    private final String validationCode;

    private final Date validationCodeExpiration;

    public ContactDetailValidationRequestedEvent(String userAccountId, String contactId, String validationCode, Date validationCodeExpiration) {
        super();
        this.userAccountId = userAccountId;
        this.contactId = contactId;
        this.validationCode = validationCode;
        this.validationCodeExpiration = validationCodeExpiration;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getContactId() {
        return contactId;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public Date getValidationCodeExpiration() {
        return validationCodeExpiration;
    }
    
}
