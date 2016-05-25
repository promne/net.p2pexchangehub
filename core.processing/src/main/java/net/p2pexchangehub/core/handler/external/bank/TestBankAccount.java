package net.p2pexchangehub.core.handler.external.bank;

import george.test.exchange.core.domain.ExternalBankType;

public class TestBankAccount extends ExternalBankAccount {

    public TestBankAccount() {
        super();
    }

    public TestBankAccount(String id, String currency, String accountNumber) {
        super(id, currency, accountNumber, ExternalBankType.TEST);
    }

    
}
