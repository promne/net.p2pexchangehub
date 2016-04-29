package george.test.exchange.core.processing.service.bank.provider.test;

import javax.enterprise.context.RequestScoped;

import es.aggregate.ExternalBankAccount;

@RequestScoped
public class MyClientRequestFilterBean {

    private ExternalBankAccount bankAccount;

    public MyClientRequestFilterBean() {
        super();
    }

    public ExternalBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(ExternalBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

}
