package esw.view;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import esw.domain.BankAccount;
import esw.domain.BankTransaction;

@Stateless
public class BankTransactionView {

    @PersistenceContext
    private EntityManager em;

    public List<BankTransaction> listBankTransactions(String bankAccountId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BankTransaction> cq = cb.createQuery(BankTransaction.class);
        Root<BankTransaction> rootEntry = cq.from(BankTransaction.class);
        CriteriaQuery<BankTransaction> all = cq.select(rootEntry);
        cq.where(cb.equal(rootEntry.get(BankTransaction.PROPERTY_BANK_ACCOUNT), new BankAccount(bankAccountId)));
        TypedQuery<BankTransaction> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }
    
}
