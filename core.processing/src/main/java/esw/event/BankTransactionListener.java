package esw.event;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import es.aggregate.value.TestBankTransactionData;
import es.event.ExternalBankTransactionCreatedEvent;
import es.event.ExternalBankTransactionStateChangedEvent;
import esw.domain.BankAccount;
import esw.domain.BankTransaction;
import george.test.exchange.core.domain.ExternalBankTransactionState;
import george.test.exchange.core.processing.util.JPAUtilsBean;

@Transactional
public class BankTransactionListener implements ReplayAware {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private JPAUtilsBean jpaUtils;
    
    public BankTransactionListener() {
        super();
    }

    @EventHandler
    public void transactionCreated(ExternalBankTransactionCreatedEvent event) {
        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setId(event.getId());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(event.getBankAccountId());
        bankTransaction.setBankAccount(bankAccount);
        bankTransaction.setAmount(event.getAmount());
        bankTransaction.setDate(event.getDate());
        bankTransaction.setFromAccount(event.getFromAccount());
        bankTransaction.setState(ExternalBankTransactionState.IMPORTED);
        bankTransaction.setReferenceInfo(event.getReferenceInfo());
        
        if (TestBankTransactionData.class.equals(event.getBankSpecificTransactionData().getClass())) {
            TestBankTransactionData specificData = (TestBankTransactionData) event.getBankSpecificTransactionData();
            bankTransaction.setDetail(specificData.getId());
        } else {
            throw new IllegalStateException("Unable to process transaction specific data for class " + event.getBankSpecificTransactionData().getClass().getName());            
        }
        
        em.persist(bankTransaction);
    }

    @EventHandler
    public void transactionStateChanged(ExternalBankTransactionStateChangedEvent event) {
        BankTransaction bankTransaction = em.find(BankTransaction.class, event.getTransactionId());
        bankTransaction.setState(event.getNewState());
        em.merge(bankTransaction);
    }
    
    @Override
    public void beforeReplay() {
        jpaUtils.deleteAll(BankTransaction.class);
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }    
}
