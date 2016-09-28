package net.p2pexchangehub.core.processing.service.bank.provider.test;

import net.p2pexchangehub.core.handler.external.bank.TestBankAccount;
import net.p2pexchangehub.core.processing.service.bank.BankProviderContext;

public class TestBankContext extends BankProviderContext<TestBankAccount> {

    private final String sessionId;

    public TestBankContext(TestBankAccount bankAccount, String sessionId) {
        super(bankAccount);
        this.sessionId = sessionId;
    }

    @Override
    protected long getTimeout() {
        return 30000;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return super.toString() + "//" + sessionId;
    }
    
}
