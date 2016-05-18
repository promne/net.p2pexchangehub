package es.command;

public class LogExternalBankAccountCommunicationCommand {

    private final String bankAccountId;

    private final String data;

    public LogExternalBankAccountCommunicationCommand(String bankAccountId, String data) {
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
