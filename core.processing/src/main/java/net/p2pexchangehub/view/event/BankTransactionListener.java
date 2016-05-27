package net.p2pexchangehub.view.event;

import javax.inject.Inject;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import george.test.exchange.core.domain.ExternalBankTransactionState;
import net.p2pexchangehub.core.aggregate.value.TestBankTransactionData;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionMatchedWithUserAccountEvent;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.domain.BankTransaction;
import net.p2pexchangehub.view.repository.BankTransactionRepository;

public class BankTransactionListener implements ReplayAware {

    @Inject
    private BankTransactionRepository repository;
    
    public BankTransactionListener() {
        super();
    }

    @EventHandler
    public void transactionCreated(ExternalBankTransactionCreatedEvent event) {
        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setId(event.getId());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(event.getBankAccountId());
        bankTransaction.setBankAccount(bankAccount);
        bankTransaction.setAmount(event.getAmount().getAmount());
        bankTransaction.setDate(event.getDate());
        bankTransaction.setFromAccount(event.getFromAccount());
        bankTransaction.setState(ExternalBankTransactionState.IMPORTED);
        bankTransaction.setReferenceInfo(event.getReferenceInfo());
        
        if (TestBankTransactionData.class.equals(event.getBankSpecificTransactionData().getClass())) {
            TestBankTransactionData specificData = (TestBankTransactionData) event.getBankSpecificTransactionData();
            bankTransaction.setDetail(specificData.getId());
        } else {
            throw new IllegalStateException("Unable to process transaction specific data for class " + event.getBankSpecificTransactionData().getClass().getName());            
        }
        
        repository.save(bankTransaction);
    }

    @EventHandler
    public void transactionMatchedWithUserAccount(ExternalBankTransactionMatchedWithUserAccountEvent event) {
        BankTransaction bankTransaction = repository.findOne(event.getTransactionId());
        bankTransaction.setState(ExternalBankTransactionState.MATCHED);
        repository.save(bankTransaction);
    }
    
    @Override
    public void beforeReplay() {
        repository.deleteAll();
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }    
}
