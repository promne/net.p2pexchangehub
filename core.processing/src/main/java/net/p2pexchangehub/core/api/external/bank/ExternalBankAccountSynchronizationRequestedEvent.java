package net.p2pexchangehub.core.api.external.bank;

public class ExternalBankAccountSynchronizationRequestedEvent {

    private final String bankAccountId;

    public ExternalBankAccountSynchronizationRequestedEvent(String bankAccountId) {
        super();
        this.bankAccountId = bankAccountId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }
    
}
