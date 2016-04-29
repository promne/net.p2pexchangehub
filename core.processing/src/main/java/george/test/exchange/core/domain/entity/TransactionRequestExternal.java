package george.test.exchange.core.domain.entity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import es.aggregate.ExternalBankAccount;
import george.test.exchange.core.domain.ExternalBankTransactionRequestState;
import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;

@Entity
public class TransactionRequestExternal extends Transaction {

    private ExternalBankAccount bankAccount;

    private String recipientAccountNumber;
    
    private String detailInfo;
    
    private int failedAttemptsCount;
    
    private ExternalBankTransactionRequestState requestState;
    public static final String REQUEST_STATE = "requestState";

    @OneToOne
    private ExternalBankTransaction externalBankTransaction;    
    
    public TransactionRequestExternal() {
        super();
    }

    public ExternalBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(ExternalBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public ExternalBankTransactionRequestState getRequestState() {
        return requestState;
    }

    public void setRequestState(ExternalBankTransactionRequestState state) {
        this.requestState = state;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public void setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    public ExternalBankTransaction getExternalBankTransaction() {
        return externalBankTransaction;
    }

    public void setExternalBankTransaction(ExternalBankTransaction externalBankTransaction) {
        this.externalBankTransaction = externalBankTransaction;
    }

    public int getFailedAttemptsCount() {
        return failedAttemptsCount;
    }

    public void setFailedAttemptsCount(int failedAttemptsCount) {
        this.failedAttemptsCount = failedAttemptsCount;
    }
    
}
