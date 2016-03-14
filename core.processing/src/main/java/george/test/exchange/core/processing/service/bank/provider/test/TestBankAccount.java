package george.test.exchange.core.processing.service.bank.provider.test;

import javax.persistence.Entity;

import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;

@Entity
public class TestBankAccount extends ExternalBankAccount {

    private String username;
    
    private String password;
        
    @Override
    public ExternalBankType getBankType() {
        return ExternalBankType.TEST;
    }

    @Override
    public String getFullAccountNumber() {
        return "PREFIX-" + getAccountNumber() + "-POSTFIX";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
