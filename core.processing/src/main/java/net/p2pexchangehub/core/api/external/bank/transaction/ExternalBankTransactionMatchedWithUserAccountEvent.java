package net.p2pexchangehub.core.api.external.bank.transaction;

public class ExternalBankTransactionMatchedWithUserAccountEvent {

    private final String transactionId;
    
    private final String userAccountId;

    public ExternalBankTransactionMatchedWithUserAccountEvent(String transactionId, String userAccountId) {
        super();
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }
        
}
