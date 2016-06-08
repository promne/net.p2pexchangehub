package net.p2pexchangehub.core.api.user.contact;

public class PhoneNumberContactAddedEvent extends ContactDetailAddedEvent {

    private final String number;

    public PhoneNumberContactAddedEvent(String userAccountId, String number) {
        super(userAccountId);
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
    
}
