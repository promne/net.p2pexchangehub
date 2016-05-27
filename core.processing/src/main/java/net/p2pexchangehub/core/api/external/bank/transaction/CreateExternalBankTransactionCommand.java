package net.p2pexchangehub.core.api.external.bank.transaction;

import java.util.Date;

import net.p2pexchangehub.core.aggregate.value.BankSpecificTransactionData;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class CreateExternalBankTransactionCommand {

    private final String bankAccountId;
    
    private final CurrencyAmount amount;
    
    private final Date date;
    
    private final String fromAccount;
    
    private final String referenceInfo;
    
    private final BankSpecificTransactionData bankSpecificTransactionData;

    public CreateExternalBankTransactionCommand(String bankAccountId, CurrencyAmount amount, Date date, String fromAccount, String referenceInfo, BankSpecificTransactionData bankSpecificTransactionData) {
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

    public CurrencyAmount getAmount() {
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
