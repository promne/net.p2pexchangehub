package george.test.exchange.core.processing.service.bank.provider.fio;

import javax.persistence.Entity;

import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;

@Entity
public class FioBankAccount extends ExternalBankAccount {

    private String username;
    
    private String password;
    
    private String deviceId;
    
    public FioBankAccount() {
        super();
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public ExternalBankType getBankType() {
        return ExternalBankType.FIO;
    }

    @Override
    public String getFullAccountNumber() {
        return getAccountNumber();
    }

}
