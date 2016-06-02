package net.p2pexchangehub.core.handler.external.bank;

import org.axonframework.domain.MetaData;

import george.test.exchange.core.domain.ExternalBankType;

public class TestBankAccount extends ExternalBankAccount {

    public TestBankAccount() {
        super();
    }

    public TestBankAccount(String id, String currency, String accountNumber, MetaData metadata) {
        super(id, currency, accountNumber, ExternalBankType.TEST, metadata);
    }

    
}
