package esw.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.eventhandling.annotation.EventHandler;

import es.event.UserAccountCreatedEvent;
import es.event.UserIncomingTransactionMatchedEvent;
import esw.domain.UserAccount;
import esw.domain.UserAccountWallet;

@Stateless
public class UserAccountListener {

    @PersistenceContext
    private EntityManager em;
    
    @EventHandler
    public void accountCreated(UserAccountCreatedEvent event) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(event.getUserAccountId());
        userAccount.setUsername(event.getUsername());
        em.persist(userAccount);
    }
    
    @EventHandler
    public void accountCreated(UserIncomingTransactionMatchedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getId());
        UserAccountWallet wallet = new UserAccountWallet(event.getCurrency(), event.getNewBalance());
        userAccount.getWallet().remove(wallet);
        userAccount.getWallet().add(wallet);
        em.merge(userAccount);
    }
    
}
