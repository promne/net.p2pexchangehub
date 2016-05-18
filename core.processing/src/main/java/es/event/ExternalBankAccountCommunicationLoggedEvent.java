package es.event;

public class ExternalBankAccountCommunicationLoggedEvent {

    private final String bankAccountId;

    private final String data;

    public ExternalBankAccountCommunicationLoggedEvent(String bankAccountId, String data) {
        super();
        this.bankAccountId = bankAccountId;
        this.data = data;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getData() {
        return data;
    }
    
}
