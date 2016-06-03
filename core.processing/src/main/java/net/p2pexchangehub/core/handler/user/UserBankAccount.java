package net.p2pexchangehub.core.handler.user;

public class UserBankAccount {

    private final String id;

    private final String country;

    private final String currency;

    private final String accountNumber;
    
    private String ownerName;

    public UserBankAccount(String id, String country, String currency, String accountNumber) {
        super();
        this.id = id;
        this.country = country;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCountry() {
        return country;
    }

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

}
