package esw.view;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;

import esw.domain.UserAccount;

@Stateless
public class UserAccountView {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Logger log;
    
    public UserAccount get(String userAccountId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> tr = cq.from(UserAccount.class);
        tr.fetch(UserAccount.PROPERTY_BANK_ACCOUNTS, JoinType.LEFT);
        tr.fetch(UserAccount.PROPERTY_WALLET, JoinType.LEFT);
        tr.fetch(UserAccount.PROPERTY_CONTACTS, JoinType.LEFT);
        cq.where(cb.equal(tr.get(UserAccount.PROPERTY_ID), userAccountId));
        return em.createQuery(cq).getSingleResult();
    }

    public Optional<UserAccount> authenticate(String username, String password, Object metadata) {
        //FIXME: turn into call to aggregate!! With additional info like ip etc.
        Optional<UserAccount> userAccount = getByUsername(username);
        if (userAccount.isPresent()) {
            if (BCrypt.checkpw(password, userAccount.get().getPasswordHash())) {
                return userAccount;
            }            
        }
        return Optional.empty();
    }

    public Optional<UserAccount> getByUsername(String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> tr = cq.from(UserAccount.class);
        cq.where(cb.equal(tr.get(UserAccount.PROPERTY_USERNAME), username));
        List<UserAccount> resultList = em.createQuery(cq).getResultList();
        if (resultList.size()>1) {
            throw new IllegalStateException("There are more users with username "+ username);
        }
        return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
    }    
    
}
