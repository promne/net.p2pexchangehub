package george.test.exchange.core.processing.service.bank;

import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import george.test.exchange.core.domain.ExternalBankTransactionRequestState;
import george.test.exchange.core.domain.ExternalBankTransactionState;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;
import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;
import george.test.exchange.core.processing.service.bank.event.MatchExternalTransactionWithOfferEvent;

@Stateless
public class BankService {
    
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Logger log;
    
    @Inject
    @Any
    private Instance<BankProvider> bankProviders;
    
    @Inject
    private Event<MatchExternalTransactionWithOfferEvent> matchExternalBankAccountTransactionEvent;
    
    public List<ExternalBankAccount> listExternalBankAccounts() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExternalBankAccount> cq = cb.createQuery(ExternalBankAccount.class);
        Root<ExternalBankAccount> rootEntry = cq.from(ExternalBankAccount.class);
        CriteriaQuery<ExternalBankAccount> all = cq.select(rootEntry);
        TypedQuery<ExternalBankAccount> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    private Optional<BankProvider> getBankProvider(ExternalBankType bankType) {
        return StreamSupport.stream(bankProviders.spliterator(), false).filter(p -> p.getType()==bankType).findAny();
    }
    
    private List<ExternalBankTransaction> listTransactions(ExternalBankAccount bankAccount, Date fromDate, Date toDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExternalBankTransaction> cq = cb.createQuery(ExternalBankTransaction.class);
        Root<ExternalBankTransaction> rootEntry = cq.from(ExternalBankTransaction.class);
        cq.where(cb.and(cb.equal(rootEntry.get(ExternalBankTransaction.BANK_ACCOUNT), bankAccount), cb.between(rootEntry.get(ExternalBankTransaction.DATE), fromDate, toDate)));
        CriteriaQuery<ExternalBankTransaction> all = cq.select(rootEntry);
        TypedQuery<ExternalBankTransaction> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    public List<ExternalBankTransaction> listTransactions(ExternalBankTransactionState state) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExternalBankTransaction> cq = cb.createQuery(ExternalBankTransaction.class);
        Root<ExternalBankTransaction> rootEntry = cq.from(ExternalBankTransaction.class);
        cq.where(cb.equal(rootEntry.get(ExternalBankTransaction.STATE), state));
        CriteriaQuery<ExternalBankTransaction> all = cq.select(rootEntry);
        TypedQuery<ExternalBankTransaction> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }
    

    public List<TransactionRequestExternal> listTransactionRequests(ExternalBankTransactionRequestState state) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TransactionRequestExternal> cq = cb.createQuery(TransactionRequestExternal.class);
        Root<TransactionRequestExternal> rootEntry = cq.from(TransactionRequestExternal.class);
        cq.where(cb.equal(rootEntry.get(TransactionRequestExternal.REQUEST_STATE), state));
        CriteriaQuery<TransactionRequestExternal> all = cq.select(rootEntry);
        TypedQuery<TransactionRequestExternal> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    public ExternalBankAccount getRandomBankAccount(String currency) {
        Optional<ExternalBankAccount> bankAccount = listExternalBankAccounts().stream().filter(a -> a.isActive() && a.getCurrency().equals(currency)).findAny();
        if (!bankAccount.isPresent()) {
            throw new IllegalStateException("There is no active account for currency "+currency);
        }
        return bankAccount.get();
    }


    /**
     * Requests transaction with external provider and persists that information.
     * 
     * @param bankContext
     * @param tre
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean requestExternalTransaction(TransactionRequestExternal tre) {
        boolean result = true;
        final int failedAttemptsLimit = 5;
        BankProvider bankProvider = getBankProvider(tre.getBankAccount().getBankType()).get();
        
        TransactionRequestExternal transactionRequest = em.find(TransactionRequestExternal.class, tre.getId());
        if (transactionRequest.getRequestState() == ExternalBankTransactionRequestState.NEW) {
            if (transactionRequest.getFailedAttemptsCount() < failedAttemptsLimit) {
                try {
                    bankProvider.processTransactionRequest(transactionRequest);
                    transactionRequest.setRequestState(ExternalBankTransactionRequestState.PENDING);
                } catch (BankProviderException e) {
                    log.error("Error during processing transaction request {} : {}", transactionRequest.getId(), e.getMessage());
                    transactionRequest.setFailedAttemptsCount(transactionRequest.getFailedAttemptsCount()+1);
                    if (transactionRequest.getFailedAttemptsCount() == failedAttemptsLimit) {
                        transactionRequest.setRequestState(ExternalBankTransactionRequestState.FAILED);
                    }
                    result = false;
                }
            }
            em.merge(transactionRequest);
        } else {
            log.debug("Unable to process external transaction request with state {}" + transactionRequest.getRequestState());
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void synchronizeExternalBankTransactions(ExternalBankAccount bankAccount) {
        BankProvider bankProvider = getBankProvider(bankAccount.getBankType()).get();
        
        try {
            List<ExternalBankTransaction> transactions = bankProvider.listTransactions(bankAccount, bankAccount.getLastCheck(), new Date());
            
            if (!transactions.isEmpty()) {
                LongSummaryStatistics dateBoundaries = transactions.stream().collect(Collectors.summarizingLong(i -> i.getDate().getTime()));
                List<ExternalBankTransaction> existingTransactions = listTransactions(bankAccount, new Date(dateBoundaries.getMin()), new Date(dateBoundaries.getMax()));
                for (ExternalBankTransaction tr : transactions) {
                    if (!existingTransactions.contains(tr)) {
                        tr.setState(ExternalBankTransactionState.IMPORTED);
                        em.persist(tr);
                        matchExternalBankAccountTransactionEvent.fire(new MatchExternalTransactionWithOfferEvent(tr));
                    }
                }
            }
            bankAccount.setBalance(bankProvider.getBalance(bankAccount));
            bankAccount.setLastCheck(new Date());
            em.merge(bankAccount);
        } catch (BankProviderException e) {
            log.error(String.format("Unable to synchronize external bank account %s : %s", bankAccount.getId(), e.getMessage()));
        }
    }

}
