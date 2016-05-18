package es.event;

public abstract class ContactDetailAddedEvent {

    private final String userAccountId;
    
    private final String contactDetailId;

    public ContactDetailAddedEvent(String userAccountId, String contactDetailId) {
        super();
        this.userAccountId = userAccountId;
        this.contactDetailId = contactDetailId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getContactDetailId() {
        return contactDetailId;
    }
    
}
