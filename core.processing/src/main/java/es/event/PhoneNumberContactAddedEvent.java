package es.event;

public class PhoneNumberContactAddedEvent extends ContactDetailAddedEvent {

    private final String number;

    public PhoneNumberContactAddedEvent(String userAccountId, String contactDetailId, String number) {
        super(userAccountId, contactDetailId);
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
    
}
