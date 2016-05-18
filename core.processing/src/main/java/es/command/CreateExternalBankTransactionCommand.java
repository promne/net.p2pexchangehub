package es.command;

import java.math.BigDecimal;
import java.util.Date;

import es.aggregate.value.BankSpecificTransactionData;

public class CreateExternalBankTransactionCommand {

    private final String bankAccountId;
    
    private final BigDecimal amount;
    
    private final Date date;
    
    private final String fromAccount;
    
    private final String referenceInfo;
    
    private final BankSpecificTransactionData bankSpecificTransactionData;

    public CreateExternalBankTransactionCommand(String bankAccountId, BigDecimal amount, Date date, String fromAccount, String referenceInfo, BankSpecificTransactionData bankSpecificTransactionData) {
        super();
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.date = date;
        this.fromAccount = fromAccount;
        this.referenceInfo = referenceInfo;
        this.bankSpecificTransactionData = bankSpecificTransactionData;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getReferenceInfo() {
        return referenceInfo;
    }

    public BankSpecificTransactionData getBankSpecificTransactionData() {
        return bankSpecificTransactionData;
    }

}
