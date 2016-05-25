package net.p2pexchangehub.core.api.user;

public class DiscardAccountDebitReservationCommand {

    private final String userAccountId;
    
    private final String transactionId;

    public DiscardAccountDebitReservationCommand(String userAccountId, String transactionId) {
        super();
        this.userAccountId = userAccountId;
        this.transactionId = transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }
    
}
