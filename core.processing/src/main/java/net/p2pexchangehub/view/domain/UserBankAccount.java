package net.p2pexchangehub.view.domain;

public class UserBankAccount {

    private String id;
    public static final String PROPERTY_ID = "id";

    private String currency;

    private String accountNumber;
    public static final String PROPERTY_ACCOUNT_NUMBER = "accountNumber";

    public UserBankAccount() {
        super();
    }

    public UserBankAccount(String id, String currency, String accountNumber) {
        this.id = id;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserBankAccount other = (UserBankAccount) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
        
}
