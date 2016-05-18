package esw.event;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import es.event.ContactDetailAddedEvent;
import es.event.ContactDetailValidatedEvent;
import es.event.EmailContactAddedEvent;
import es.event.PhoneNumberContactAddedEvent;
import es.event.UserAccountCreatedEvent;
import es.event.UserAccountPasswordChangedEvent;
import es.event.UserAccountRolesAddedEvent;
import es.event.UserAccountRolesRemovedEvent;
import es.event.UserAccountStateChangedEvent;
import es.event.UserBankAccountCreatedEvent;
import es.event.UserIncomingTransactionMatchedEvent;
import esw.domain.UserAccount;
import esw.domain.UserAccountContact;
import esw.domain.UserAccountContact.Type;
import esw.domain.UserAccountWallet;
import esw.domain.UserBankAccount;
import george.test.exchange.core.domain.UserAccountState;
import george.test.exchange.core.processing.util.JPAUtilsBean;

@Transactional
public class UserAccountListener implements ReplayAware {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private JPAUtilsBean jpaUtils;
    
    @EventHandler
    public void accountCreated(UserAccountCreatedEvent event) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(event.getUserAccountId());
        userAccount.setUsername(event.getUsername());
        em.persist(userAccount);
    }
    
    @EventHandler
    public void handleUserIncomingTransactionMatched(UserIncomingTransactionMatchedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        UserAccountWallet wallet = new UserAccountWallet(event.getCurrency(), event.getNewBalance());
        userAccount.getWallet().remove(wallet);
        userAccount.getWallet().add(wallet);
        em.merge(userAccount);
    }
    
    @EventHandler
    public void handleUserBankAccountCreatedEvent(UserBankAccountCreatedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        UserBankAccount bankAccount = new UserBankAccount(event.getBankAccount().getId(), event.getBankAccount().getCurrency(), event.getBankAccount().getAccountNumber());
        userAccount.getBankAccounts().add(bankAccount);
        em.merge(userAccount);        
    }
    
    @EventHandler
    public void handle(UserAccountPasswordChangedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        userAccount.setPasswordHash(event.getNewPasswordHash());
        em.merge(userAccount);
    }

    @EventHandler
    public void handle(UserAccountStateChangedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        userAccount.setEnabled(UserAccountState.ACTIVE==event.getNewState());
        em.merge(userAccount);
    }

    @EventHandler
    public void handle(UserAccountRolesAddedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        userAccount.getRoles().addAll(event.getRoles());
        em.merge(userAccount);
    }

    @EventHandler
    public void handle(UserAccountRolesRemovedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        userAccount.getRoles().removeAll(event.getRoles());
        em.merge(userAccount);
    }

    @EventHandler
    public void handle(ContactDetailAddedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        
        UserAccountContact newContact;
        if (PhoneNumberContactAddedEvent.class.equals(event.getClass())) {
            newContact = new UserAccountContact(event.getContactDetailId(), ((PhoneNumberContactAddedEvent)event).getNumber(), Type.PHONE);
        } else if (EmailContactAddedEvent.class.equals(event.getClass())) {
            newContact = new UserAccountContact(event.getContactDetailId(), ((EmailContactAddedEvent)event).getEmailAddress(), Type.EMAIL);
        } else {
            throw new IllegalStateException("Unable to handle " + event.getClass());
        }
        userAccount.getContacts().put(newContact.getId(), newContact);
    }

    @EventHandler
    public void handle(ContactDetailValidatedEvent event) {
        UserAccount userAccount = em.find(UserAccount.class, event.getUserAccountId());
        userAccount.getContacts().get(event.getContactId()).setValidated(true);
        em.merge(userAccount);
    }
    
    @Override
    public void beforeReplay() {
        jpaUtils.deleteAll(UserAccount.class);
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }    
    
}
