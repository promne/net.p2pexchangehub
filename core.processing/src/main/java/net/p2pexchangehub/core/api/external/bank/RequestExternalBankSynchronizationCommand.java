package net.p2pexchangehub.core.api.external.bank;

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
