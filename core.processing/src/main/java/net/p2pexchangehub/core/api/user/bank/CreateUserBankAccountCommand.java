package net.p2pexchangehub.core.api.user.bank;

public class CreateUserBankAccountCommand {

    private final String userAccountId;

    private final String currency;

    private final String accountNumber;

    private final String accountOwnerName;

    public CreateUserBankAccountCommand(String userAccountId,String currency, String accountNumber, String accountOwnerName) {
        super();
        this.userAccountId = userAccountId;
        this.currency = currency;
        this.accountNumber = accountNumber;
        this.accountOwnerName = accountOwnerName;
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

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

}
