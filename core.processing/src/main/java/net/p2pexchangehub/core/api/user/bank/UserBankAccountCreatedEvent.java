package net.p2pexchangehub.core.api.user.bank;

public class UserBankAccountCreatedEvent {

    private final String userAccountId;

    private final String bankAccountId;

    private final String country;
    
    private final String currency;
    
    private final String accountNumber;

    public UserBankAccountCreatedEvent(String userAccountId, String bankAccountId, String country, String currency, String accountNumber) {
        super();
        this.userAccountId = userAccountId;
        this.bankAccountId = bankAccountId;
        this.country = country;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    
}
