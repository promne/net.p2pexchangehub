package net.p2pexchangehub.core.api.user.bank;

public class UserBankAccountOwnerNameChangedEvent {

    private final String userAccountId;

    private final String currency;
    
    private final String accountNumber;
    
    private final String ownerName;

    public UserBankAccountOwnerNameChangedEvent(String userAccountId, String currency, String accountNumber, String ownerName) {
        super();
        this.userAccountId = userAccountId;
        this.currency = currency;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getOwnerName() {
        return ownerName;
    }
        
}
