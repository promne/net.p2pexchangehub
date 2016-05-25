package net.p2pexchangehub.core.handler.external.bank.transaction;

import java.math.BigDecimal;
import java.util.Date;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import george.test.exchange.core.domain.ExternalBankTransactionState;
import net.p2pexchangehub.core.aggregate.value.BankSpecificTransactionData;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionMatchedWithUserAccountEvent;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionStateChangedEvent;

public class ExternalBankTransaction extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String bankAccountId;
    
    private BigDecimal amount;

    private Date date;

    private ExternalBankTransactionState state;

    public ExternalBankTransaction() {
        super();
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }
    
    public String getBankAccountId() {
        return bankAccountId;
    }

    public ExternalBankTransaction(String id, String bankAccountId, BigDecimal amount, Date date, String fromAccount, String referenceInfo, BankSpecificTransactionData bankSpecificTransactionData) {
        super();
        apply(new ExternalBankTransactionCreatedEvent(id, bankAccountId, amount, date, fromAccount, referenceInfo, bankSpecificTransactionData));
        apply(new ExternalBankTransactionStateChangedEvent(id, ExternalBankTransactionState.IMPORTED));
    }

    @EventHandler
    private void handleCreated(ExternalBankTransactionCreatedEvent event) {
        amount = event.getAmount();
        bankAccountId = event.getBankAccountId();
        date = event.getDate();
        id = event.getId();
    }

    @EventHandler
    private void handleStateChanged(ExternalBankTransactionStateChangedEvent event) {
        this.state = event.getNewState();
    }

    public void matchWith(String userAccountId) {
        if (state != ExternalBankTransactionState.IMPORTED) {
            throw new IllegalStateException("Unable to match transaction "+ id + " with state " + state);
        }
        apply(new ExternalBankTransactionMatchedWithUserAccountEvent(id, userAccountId));
        apply(new ExternalBankTransactionStateChangedEvent(id, ExternalBankTransactionState.MATCHED));
    }
    
    @EventHandler
    private void handleTransactionMatchedWithUserAccount(ExternalBankTransactionMatchedWithUserAccountEvent event) {
        // nothing
    }
     
}

