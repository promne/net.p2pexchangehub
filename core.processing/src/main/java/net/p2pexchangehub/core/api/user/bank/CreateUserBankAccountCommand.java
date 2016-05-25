package net.p2pexchangehub.core.api.user.bank;

public class CreateUserBankAccountCommand {

    private final String userAccountId;

    private final String currency;

    private final String accountNumber;

    public CreateUserBankAccountCommand(String userAccountId, String currency, String accountNumber) {
        super();
        this.userAccountId = userAccountId;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

}
