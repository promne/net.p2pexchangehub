package net.p2pexchangehub.core.processing.service.bank.provider.test;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class MyClientRequestFilterBean {

    private String bankAccountId;

    public MyClientRequestFilterBean() {
        super();
    }

    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

}
