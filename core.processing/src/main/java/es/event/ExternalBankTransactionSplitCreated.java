package es.event;

import es.aggregate.value.TransactionSplit;

public class ExternalBankTransactionSplitCreated {

    private final String externalBankTransactionId;
    
    private final TransactionSplit transactionSplit;

    public ExternalBankTransactionSplitCreated(String externalBankTransactionId, TransactionSplit transactionSplit) {
        super();
        this.externalBankTransactionId = externalBankTransactionId;
        this.transactionSplit = transactionSplit;
    }

    public String getExternalBankTransactionId() {
        return externalBankTransactionId;
    }

    public TransactionSplit getTransactionSplit() {
        return transactionSplit;
    }
    
}
