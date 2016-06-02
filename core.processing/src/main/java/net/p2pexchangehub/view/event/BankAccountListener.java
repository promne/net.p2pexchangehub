package net.p2pexchangehub.view.event;

import javax.inject.Inject;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.annotation.Timestamp;
import org.axonframework.eventhandling.replay.ReplayAware;
import org.joda.time.DateTime;

import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountActiveSetEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCommunicationLoggedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountSynchronizationEnabledSetEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountSynchronizedEvent;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.domain.BankCommunication;
import net.p2pexchangehub.view.repository.BankAccountRepository;
import net.p2pexchangehub.view.repository.BankCommunicationRepository;

public class BankAccountListener implements ReplayAware {

    @Inject
    private BankAccountRepository bankAccountRepository;
    
    @Inject
    private BankCommunicationRepository bankCommunicationRepository;
    
    @EventHandler
    public void accountCreated(ExternalBankAccountCreatedEvent event) {
        BankAccount account = new BankAccount();
        account.setAccountNumber(event.getAccountNumber());
        account.setId(event.getBankAccountId());
        account.setBankType(event.getBankType());
        account.setCurrency(event.getCurrency());
        bankAccountRepository.save(account);
    }
    
    @EventHandler
    public void handleSynchronized(ExternalBankAccountSynchronizedEvent event) {
        BankAccount bankAccount = bankAccountRepository.findOne(event.getBankAccountId());
        bankAccount.setLastCheck(event.getSyncDate());
        bankAccount.setBalance(event.getBalance());
        bankAccountRepository.save(bankAccount);
    }
    
    @EventHandler
    public void handleActiveSet(ExternalBankAccountActiveSetEvent event) {
        BankAccount bankAccount = bankAccountRepository.findOne(event.getBankAccountId());
        bankAccount.setActive(event.isActive());
        bankAccountRepository.save(bankAccount);        
    }

    @EventHandler
    public void handleSynchronizationEnabledSet(ExternalBankAccountSynchronizationEnabledSetEvent event) {
        BankAccount bankAccount = bankAccountRepository.findOne(event.getBankAccountId());
        bankAccount.setSynchronizationEnabled(event.isEnabled());
        bankAccountRepository.save(bankAccount);        
    }

    @EventHandler
    public void handleLogCommunication(ExternalBankAccountCommunicationLoggedEvent event, @Timestamp DateTime timestamp) {
        BankCommunication entity = new BankCommunication(timestamp.toDate(), event.getBankAccountId(), event.getData());
        bankCommunicationRepository.save(entity);
    }

    @Override
    public void beforeReplay() {
        bankCommunicationRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
    
}
