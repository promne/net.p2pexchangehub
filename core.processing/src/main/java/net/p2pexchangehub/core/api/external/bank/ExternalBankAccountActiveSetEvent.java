package net.p2pexchangehub.core.api.external.bank;

public class ExternalBankAccountActiveSetEvent {

    private final String bankAccountId;

    private final boolean active;

    public ExternalBankAccountActiveSetEvent(String bankAccountId, boolean active) {
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
