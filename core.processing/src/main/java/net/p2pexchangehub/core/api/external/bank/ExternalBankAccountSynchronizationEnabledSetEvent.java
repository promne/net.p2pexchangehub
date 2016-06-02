package net.p2pexchangehub.core.api.external.bank;

public class ExternalBankAccountSynchronizationEnabledSetEvent {

    private final String bankAccountId;

    private final boolean enabled;

    public ExternalBankAccountSynchronizationEnabledSetEvent(String bankAccountId, boolean enabled) {
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
