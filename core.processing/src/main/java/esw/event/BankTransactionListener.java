package esw.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.eventhandling.annotation.EventHandler;

import es.event.ExternalBankTransactionCreatedEvent;
import es.event.ExternalBankTransactionStateChangedEvent;
import esw.domain.BankAccount;
import esw.domain.BankTransaction;
import george.test.exchange.core.domain.ExternalBankTransactionState;

@Stateless
public class BankTransactionListener {

    @PersistenceContext
    private EntityManager em;
    
    @EventHandler
    public void transactionCreated(ExternalBankTransactionCreatedEvent event) {
        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setId(event.getId());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(event.getBankAccountId());
        bankTransaction.setBankAccount(bankAccount);
        
        
        bankTransaction.setAmount(event.getAmount());
        bankTransaction.setDate(event.getDate());
        bankTransaction.setState(ExternalBankTransactionState.IMPORTED);
        bankTransaction.setDetail(event.getDetailInfo() + " // " + event.getExternalId() + " // " + event.getFromAccount());
        em.persist(bankTransaction);
    }

    @EventHandler
    public void transactionCreated(ExternalBankTransactionStateChangedEvent event) {
        BankTransaction bankTransaction = em.find(BankTransaction.class, event.getTransactionId());
        bankTransaction.setState(event.getNewState());
        em.merge(bankTransaction);
    }
    
}
