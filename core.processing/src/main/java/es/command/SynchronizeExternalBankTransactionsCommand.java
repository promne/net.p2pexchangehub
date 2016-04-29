package es.command;

public class SynchronizeExternalBankTransactionsCommand {

    private final String bankAccountId;

    public SynchronizeExternalBankTransactionsCommand(String bankAccountId) {
        super();
        this.bankAccountId = bankAccountId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }
    
}
