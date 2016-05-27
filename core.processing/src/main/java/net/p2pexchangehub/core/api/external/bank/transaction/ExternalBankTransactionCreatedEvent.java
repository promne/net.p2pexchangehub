package net.p2pexchangehub.core.api.external.bank.transaction;

import java.util.Date;

import net.p2pexchangehub.core.aggregate.value.BankSpecificTransactionData;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class ExternalBankTransactionCreatedEvent {

    private final String id;

    private final String bankAccountId;
    
    private final CurrencyAmount amount;

    private final String fromAccount;
    
    private final Date date;

    private final String referenceInfo;

    private final BankSpecificTransactionData bankSpecificTransactionData;

    public ExternalBankTransactionCreatedEvent(String id, String bankAccountId, CurrencyAmount amount, Date date, String fromAccount, String referenceInfo, BankSpecificTransactionData bankSpecificTransactionData) {
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

    public CurrencyAmount getAmount() {
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
