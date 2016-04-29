package es.event;

public class ExternalBankAccountCredentialsSetEvent {

    private final String bankAccountId;

    private final String username;

    private final String password;

    public ExternalBankAccountCredentialsSetEvent(String bankAccountId, String username, String password) {
        super();
        this.bankAccountId = bankAccountId;
        this.username = username;
        this.password = password;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
