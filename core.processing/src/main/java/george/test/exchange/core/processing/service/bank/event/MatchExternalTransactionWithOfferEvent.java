package george.test.exchange.core.processing.service.bank.event;

import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;

public class MatchExternalTransactionWithOfferEvent {

    private final ExternalBankTransaction bankTransaction;

    public MatchExternalTransactionWithOfferEvent(ExternalBankTransaction bankTransaction) {
        super();
        this.bankTransaction = bankTransaction;
    }

    public ExternalBankTransaction getBankTransaction() {
        return bankTransaction;
    }
    
}
