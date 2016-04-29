package es.event;

import george.test.exchange.core.domain.ExternalBankTransactionState;

public class ExternalBankTransactionStateChangedEvent {

    private final String transactionId;
    
    private final ExternalBankTransactionState newState;

    public ExternalBankTransactionStateChangedEvent(String transactionId, ExternalBankTransactionState newState) {
        super();
        this.transactionId = transactionId;
        this.newState = newState;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public ExternalBankTransactionState getNewState() {
        return newState;
    }
        
}
