package george.test.exchange.core.processing.service.bank;

import java.util.Date;

import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccount;

public abstract class BankProviderContext<A extends ExternalBankAccount> {

    private A bankAccount;

    private boolean active = true;
    
    private volatile Date lastUsed;
    
    public BankProviderContext(A bankAccount) {
        super();
        this.bankAccount = bankAccount;
        this.lastUsed = new Date();
    }

    void setBankAccount(A bankAccount) {
        this.bankAccount = bankAccount;
    }
    
    /* in miliseconds */
    protected abstract long getTimeout();
    
    public A getBankAccount() {
        return bankAccount;
    }

    public void deactivate() {
        active = false;
    }
    
    public boolean isActive() {
        return active && ((new Date()).getTime()-getTimeout())<lastUsed.getTime();
    }
    
    public void touch() {
        lastUsed = new Date();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "//" + bankAccount.getId();
    }

}
