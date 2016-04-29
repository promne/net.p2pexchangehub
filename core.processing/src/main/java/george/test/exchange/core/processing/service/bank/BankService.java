package george.test.exchange.core.processing.service.bank;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

import esw.domain.BankAccount;
import george.test.exchange.core.domain.ExternalBankTransactionRequestState;
import george.test.exchange.core.domain.ExternalBankTransactionState;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;

@Stateless
public class BankService {
    
    @PersistenceContext
    private EntityManager em;

    @Inject
    private Logger log;
    
    @Inject
    @Any
    private Instance<BankProvider> bankProviders;
    
    public List<BankAccount> listBankAccounts() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BankAccount> cq = cb.createQuery(BankAccount.class);
        Root<BankAccount> rootEntry = cq.from(BankAccount.class);
        CriteriaQuery<BankAccount> all = cq.select(rootEntry);
        TypedQuery<BankAccount> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    private Optional<BankProvider> getBankProvider(ExternalBankType bankType) {
        return StreamSupport.stream(bankProviders.spliterator(), false).filter(p -> p.getType()==bankType).findAny();
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
        BankProvider bankProvider = null; // = getBankProvider(tre.getBankAccount().getBankType()).get();
        
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


}
