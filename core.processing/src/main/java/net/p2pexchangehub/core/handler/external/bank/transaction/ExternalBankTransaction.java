package net.p2pexchangehub.core.handler.external.bank.transaction;

import java.util.Date;

import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import net.p2pexchangehub.core.aggregate.value.BankSpecificTransactionData;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionMatchedWithUserAccountEvent;

public class ExternalBankTransaction extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String bankAccountId;
    
    private CurrencyAmount amount;

    private Date date;
    
    private String referenceInfo;

    private boolean matchedWithUserAccount;

    public ExternalBankTransaction() {
        super();
    }

    public String getId() {
        return id;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }
    
    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getReferenceInfo() {
        return referenceInfo;
    }

    public ExternalBankTransaction(String id, String bankAccountId, CurrencyAmount amount, Date date, String fromAccount, String referenceInfo, BankSpecificTransactionData bankSpecificTransactionData, MetaData metadata) {
        super();
        apply(new ExternalBankTransactionCreatedEvent(id, bankAccountId, amount, date, fromAccount, referenceInfo, bankSpecificTransactionData), metadata);
    }

    @EventHandler
    private void handleCreated(ExternalBankTransactionCreatedEvent event) {
        amount = event.getAmount();
        bankAccountId = event.getBankAccountId();
        date = event.getDate();
        id = event.getId();
        referenceInfo = event.getReferenceInfo();
        matchedWithUserAccount = false;
    }


    public void matchWith(String userAccountId, MetaData metadata) {
        if (matchedWithUserAccount) {
            throw new IllegalStateException("Unable to match transaction "+ id);
        }
        apply(new ExternalBankTransactionMatchedWithUserAccountEvent(id, userAccountId), metadata);
    }
    
    @EventHandler
    private void handleTransactionMatchedWithUserAccount(ExternalBankTransactionMatchedWithUserAccountEvent event) {
        matchedWithUserAccount = true;
    }
     
}

