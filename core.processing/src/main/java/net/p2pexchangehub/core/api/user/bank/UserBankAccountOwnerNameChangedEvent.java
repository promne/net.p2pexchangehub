package net.p2pexchangehub.core.api.user.bank;

public class UserBankAccountOwnerNameChangedEvent {

    private final String userAccountId;

    private final String bankAccountId;
    
    private final String ownerName;

    public UserBankAccountOwnerNameChangedEvent(String userAccountId, String bankAccountId, String ownerName) {
        super();
        this.userAccountId = userAccountId;
        this.bankAccountId = bankAccountId;
        this.ownerName = ownerName;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getOwnerName() {
        return ownerName;
    }
        
}
