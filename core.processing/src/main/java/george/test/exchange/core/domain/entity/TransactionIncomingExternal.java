package george.test.exchange.core.domain.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;

@Entity
public class TransactionIncomingExternal extends Transaction {

    @ManyToOne(optional=false)
    private ExternalBankTransaction externalBankTransaction;

    public TransactionIncomingExternal() {
        super();
    }

    public ExternalBankTransaction getExternalBankTransaction() {
        return externalBankTransaction;
    }

    public void setExternalBankTransaction(ExternalBankTransaction externalBankTransaction) {
        this.externalBankTransaction = externalBankTransaction;
    }
    
}
