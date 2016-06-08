package net.p2pexchangehub.core.api.user.contact;

public class ContactDetailRemovedEvent {

    private final String userAccountId;

    private final String contactValue;

    public ContactDetailRemovedEvent(String userAccountId, String contactValue) {
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
