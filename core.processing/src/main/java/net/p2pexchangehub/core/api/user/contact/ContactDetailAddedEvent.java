package net.p2pexchangehub.core.api.user.contact;

public abstract class ContactDetailAddedEvent {

    private final String userAccountId;
    
    public ContactDetailAddedEvent(String userAccountId) {
        super();
        this.userAccountId = userAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

}
