package net.p2pexchangehub.core.api.external.bank.transaction;

public class MatchOutgoingExternalBankTransactionWithRequestedCommand {

    private final String externalTransactionId;
    
    public MatchOutgoingExternalBankTransactionWithRequestedCommand(String externalTransactionId) {
        super();
        this.externalTransactionId = externalTransactionId;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

}
