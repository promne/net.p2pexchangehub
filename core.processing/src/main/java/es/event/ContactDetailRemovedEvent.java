package es.event;

public class ContactDetailRemovedEvent {

    private final String userAccountId;

    private final String contactId;

    public ContactDetailRemovedEvent(String userAccountId, String contactId) {
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
