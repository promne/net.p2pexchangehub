package george.test.exchange.core.processing.service.bank.event;

import java.util.List;

import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;

public class ExternalBankAccountRequestEvent {

    private final ExternalBankAccount externalBankAccount;
    
    private final List<TransactionRequestExternal> requestedTransactions;

    public ExternalBankAccountRequestEvent(ExternalBankAccount externalBankAccount, List<TransactionRequestExternal> requestedTransactions) {
        super();
        this.externalBankAccount = externalBankAccount;
        this.requestedTransactions = requestedTransactions;
    }

    public ExternalBankAccount getExternalBankAccount() {
        return externalBankAccount;
    }

    public List<TransactionRequestExternal> getRequestedTransactions() {
        return requestedTransactions;
    }
    
}
