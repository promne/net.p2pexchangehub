package net.p2pexchangehub.core.api.user.contact;

public class EmailContactAddedEvent extends ContactDetailAddedEvent {

    private final String emailAddress;

    public EmailContactAddedEvent(String userAccountId, String contactDetailId, String emailAddress) {
        super(userAccountId, contactDetailId);
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
    
}
