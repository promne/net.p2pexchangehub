package es.command;

public class RequestExternalBankSynchronizationCommand {

    private final String bankAccountId;

    public RequestExternalBankSynchronizationCommand(String bankAccountId) {
        super();
        this.bankAccountId = bankAccountId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }
    
}
