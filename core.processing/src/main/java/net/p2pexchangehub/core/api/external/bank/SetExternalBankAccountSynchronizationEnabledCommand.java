package net.p2pexchangehub.core.api.external.bank;

public class SetExternalBankAccountSynchronizationEnabledCommand {

    private final String bankAccountId;

    private final boolean enabled;

    public SetExternalBankAccountSynchronizationEnabledCommand(String bankAccountId, boolean enabled) {
        super();
        this.bankAccountId = bankAccountId;
        this.enabled = enabled;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public boolean isEnabled() {
        return enabled;
    }
    
}
