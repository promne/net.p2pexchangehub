package es.event;

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
