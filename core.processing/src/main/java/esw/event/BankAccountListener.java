package esw.event;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.annotation.Timestamp;
import org.axonframework.eventhandling.replay.ReplayAware;
import org.joda.time.DateTime;

import es.event.ExternalBankAccountActiveSetEvent;
import es.event.ExternalBankAccountCommunicationLoggedEvent;
import es.event.ExternalBankAccountCreatedEvent;
import es.event.ExternalBankAccountSynchronizedEvent;
import esw.domain.BankAccount;
import esw.domain.BankCommunication;
import george.test.exchange.core.processing.util.JPAUtilsBean;

@Transactional
public class BankAccountListener implements ReplayAware {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private JPAUtilsBean jpaUtils;
    
    @EventHandler
    public void accountCreated(ExternalBankAccountCreatedEvent event) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(event.getAccountNumber());
        account.setId(event.getBankAccountId());
        account.setBankType(event.getBankType());
        account.setCurrency(event.getCurrency());
        em.persist(account);
    }
    
    @EventHandler
    public void handleSynchronized(ExternalBankAccountSynchronizedEvent event) {
        BankAccount bankAccount = em.find(BankAccount.class, event.getBankAccountId());
        bankAccount.setLastCheck(event.getSyncDate());
        bankAccount.setBalance(event.getBalance());
        em.merge(bankAccount);
    }
    
    @EventHandler
    public void handleActiveSet(ExternalBankAccountActiveSetEvent event) {
        BankAccount bankAccount = em.find(BankAccount.class, event.getBankAccountId());
        bankAccount.setActive(event.isActive());
        em.merge(bankAccount);        
    }

    @EventHandler
    public void handleLogCommunication(ExternalBankAccountCommunicationLoggedEvent event, @Timestamp DateTime timestamp) {
        BankCommunication entity = new BankCommunication(timestamp.toDate(), new BankAccount(event.getBankAccountId()), event.getData());
        em.persist(entity);
    }

    @Override
    public void beforeReplay() {
        jpaUtils.deleteAll(BankCommunication.class);
        jpaUtils.deleteAll(BankAccount.class);
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
    
}
