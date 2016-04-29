package es.command;

public class SetOwnerAccountNumberForOfferCommand {

    private final String offerId;
    
    private final String accountNumber;

    public SetOwnerAccountNumberForOfferCommand(String offerId, String accountNumber) {
        super();
        this.offerId = offerId;
        this.accountNumber = accountNumber;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }    
        
}
