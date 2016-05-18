package es.aggregate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import es.aggregate.value.BankSpecificTransactionData;
import es.aggregate.value.TransactionSplit;
import es.aggregate.value.TransactionSplit.PartnerType;
import es.event.ExternalBankTransactionCreatedEvent;
import es.event.ExternalBankTransactionSplitCreated;
import es.event.ExternalBankTransactionStateChangedEvent;
import george.test.exchange.core.domain.ExternalBankTransactionState;

public class ExternalBankTransaction extends AbstractAnnotatedAggregateRoot<String> {

    @AggregateIdentifier
    private String id;

    private String bankAccountId;
    
    private BigDecimal amount;

    private String fromAccount;
    
    private Date date;

    private ExternalBankTransactionState state;

    private List<TransactionSplit> transactionSplits = new ArrayList<>();
    
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

    public void matchWith(String partnerId, PartnerType partnerType, BigDecimal amount) {
        BigDecimal newAmountBalance = transactionSplits.stream().map(TransactionSplit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).add(amount);
        if (newAmountBalance.abs().compareTo(this.amount.abs())>0) {
            throw new IllegalArgumentException("Requested amount is bigger than available");
        }
        apply(new ExternalBankTransactionSplitCreated(this.id, new TransactionSplit(UUID.randomUUID().toString(), amount, partnerId, partnerType)));
        
        ExternalBankTransactionState newState = (newAmountBalance.abs().compareTo(this.amount.abs())==0) ? ExternalBankTransactionState.MATCHED : ExternalBankTransactionState.PARTIAL_MATCH;
        if (newState!=state) {
            apply(new ExternalBankTransactionStateChangedEvent(id, newState));
        }
    }
    
    @EventHandler
    private void handleSplitCreated(ExternalBankTransactionSplitCreated event) {
        transactionSplits.add(event.getTransactionSplit());
    }
     
}

