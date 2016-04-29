package es.event;

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
