package net.p2pexchangehub.core.handler.external.bank;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;

import george.test.exchange.core.processing.service.bank.BankProviderException;
import net.p2pexchangehub.core.api.external.bank.CreateExternalBankAccountCommand;
import net.p2pexchangehub.core.api.external.bank.LogExternalBankAccountCommunicationCommand;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankSynchronizationCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountActiveCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountCredentialsCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountSynchronizedCommand;

@Singleton
public class ExternalBankAccountCommandHandler {

    @Inject
    private Logger log;
    
    @Inject
    private Repository<TestBankAccount> repositoryAccounts;

    public Repository<TestBankAccount> getRepositoryAccounts() {
        return repositoryAccounts;
    }
    
    public void setRepositoryAccounts(Repository<TestBankAccount> repository) {
        this.repositoryAccounts = repository;
    }
    
    @CommandHandler
    public void handleCreateExternalBankAccount(CreateExternalBankAccountCommand command) {
        TestBankAccount account;
        switch (command.getBankType()) {
            case TEST:
                account = new TestBankAccount(command.getBankAccountId(), command.getCurrency(), command.getAccountNumber());
                break;
            default:
                throw new IllegalStateException("Unable to initialize account with type " + command.getBankType());
        }
        repositoryAccounts.add(account);
    }

    @CommandHandler
    public void handleSetCredentials(SetExternalBankAccountCredentialsCommand command) {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
        bankAccount.setCredentials(command.getUsername(), command.getPassword());
    }

    @CommandHandler
    public void handleSynchronizeTransactions(RequestExternalBankSynchronizationCommand command) throws BankProviderException {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
        bankAccount.requestSynchronization();
    }

    @CommandHandler
    public void handleSynchronizeTransactions(SetExternalBankAccountSynchronizedCommand command) {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
        bankAccount.setSynchronized(command.getSyncDate(), command.getBalance());        
    }
    
    @CommandHandler
    public void handleSetActive(SetExternalBankAccountActiveCommand command) {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
        bankAccount.setActive(command.isActive());
    }

    @CommandHandler
    public void handleLogCommunication(LogExternalBankAccountCommunicationCommand command) {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
        bankAccount.logCommunication(command.getData());
    }

}
