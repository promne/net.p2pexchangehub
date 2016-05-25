package george.test.exchange.core.processing.service.bank.provider.test;

import george.test.exchange.core.processing.service.bank.BankProviderContext;
import net.p2pexchangehub.core.handler.external.bank.TestBankAccount;

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
