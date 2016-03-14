package george.test.exchange.core.processing.processor;

import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;
import george.test.exchange.core.processing.service.bank.BankService;
import george.test.exchange.core.processing.service.bank.event.ExternalBankAccountRequestEvent;

@Singleton
public class ExternalBankProcessor {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private Logger log;
    
    @Inject
    private BankService bankService;
    
    @Asynchronous
    @Lock
    public void processExternalBankAccountEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) ExternalBankAccountRequestEvent externalBankAccountRequestEvent) {
        ExternalBankAccount bankAccount = em.find(ExternalBankAccount.class, externalBankAccountRequestEvent.getExternalBankAccount().getId());
        
        final int failedAttemptsLimit = 5;
        int failedTransferAttempts = 0;
        for (TransactionRequestExternal tre : externalBankAccountRequestEvent.getRequestedTransactions()) {
            if (bankService.requestExternalTransaction(tre)) {
                failedTransferAttempts++;
            }
            if (failedTransferAttempts >= failedAttemptsLimit) {
                log.error("Too many failed transaction requests for external bank account {}", bankAccount.getId());
                break;
            }
        }
        
        bankService.synchronizeExternalBankTransactions(bankAccount);
    }  
    
}
