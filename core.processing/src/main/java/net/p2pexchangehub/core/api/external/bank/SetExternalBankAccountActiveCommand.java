package net.p2pexchangehub.core.api.external.bank;

public class SetExternalBankAccountActiveCommand {

    private final String bankAccountId;

    private final boolean active;

    public SetExternalBankAccountActiveCommand(String bankAccountId, boolean active) {
        super();
        this.bankAccountId = bankAccountId;
        this.active = active;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public boolean isActive() {
        return active;
    }
    
}
