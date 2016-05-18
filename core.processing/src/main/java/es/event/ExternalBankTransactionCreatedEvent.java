package es.event;

import java.math.BigDecimal;
import java.util.Date;

import es.aggregate.value.BankSpecificTransactionData;

public class ExternalBankTransactionCreatedEvent {

    private final String id;

    private final String bankAccountId;
    
    private final BigDecimal amount;

    private final String fromAccount;
    
    private final Date date;

    private final String referenceInfo;

    private final BankSpecificTransactionData bankSpecificTransactionData;

    public ExternalBankTransactionCreatedEvent(String id, String bankAccountId, BigDecimal amount, Date date, String fromAccount, String referenceInfo, BankSpecificTransactionData bankSpecificTransactionData) {
        super();
        this.id = id;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.date = date;
        this.fromAccount = fromAccount;
        this.referenceInfo = referenceInfo;
        this.bankSpecificTransactionData = bankSpecificTransactionData;
    }

    public String getId() {
        return id;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public Date getDate() {
        return date;
    }

    public String getReferenceInfo() {
        return referenceInfo;
    }

    public BankSpecificTransactionData getBankSpecificTransactionData() {
        return bankSpecificTransactionData;
    }

}
