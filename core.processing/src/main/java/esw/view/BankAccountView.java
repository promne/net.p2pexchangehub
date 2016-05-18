package esw.view;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import esw.domain.BankAccount;

@Stateless
public class BankAccountView {

    @PersistenceContext
    private EntityManager em;

    public List<BankAccount> listBankAccounts() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BankAccount> cq = cb.createQuery(BankAccount.class);
        Root<BankAccount> rootEntry = cq.from(BankAccount.class);
        CriteriaQuery<BankAccount> all = cq.select(rootEntry);
        TypedQuery<BankAccount> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    public List<BankAccount> listActiveAccounts(String currency) {
        return listBankAccounts().stream().filter(a -> a.isActive() && a.getCurrency().equals(currency)).collect(Collectors.toList());
    }

    public Optional<BankAccount> getBankAccount(String bankAccountId) {
        return Optional.ofNullable(em.find(BankAccount.class, bankAccountId));
    }

    public Set<String> listAvailableCurrencies() {
        return listBankAccounts().stream().filter(BankAccount::isActive).map(BankAccount::getCurrency).collect(Collectors.toSet());
    }

}
