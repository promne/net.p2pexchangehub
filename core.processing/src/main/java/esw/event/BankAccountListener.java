package esw.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.annotation.Timestamp;
import org.joda.time.DateTime;

import es.event.ExternalBankAccountActiveSetEvent;
import es.event.ExternalBankAccountCommunicationLoggedEvent;
import es.event.ExternalBankAccountCreatedEvent;
import es.event.ExternalBankAccountSynchronizedEvent;
import esw.domain.BankAccount;
import esw.domain.BankCommunication;

@Stateless
public class BankAccountListener {

    @PersistenceContext
    private EntityManager em;
    
    @EventHandler
    public void accountCreated(ExternalBankAccountCreatedEvent event) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(event.getAccountNumber());
        account.setId(event.getBankAccountId());
        account.setBankType(event.getBankType());
        account.setCountry(event.getCountry());
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
    
}
