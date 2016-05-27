package net.p2pexchangehub.core.handler.external.bank;

import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;

import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.processing.service.bank.BankProvider;
import george.test.exchange.core.processing.service.bank.BankProviderException;
import net.p2pexchangehub.core.api.external.bank.CreateExternalBankAccountCommand;
import net.p2pexchangehub.core.api.external.bank.LogExternalBankAccountCommunicationCommand;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankSynchronizationCommand;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankTransactionCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountActiveCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountCredentialsCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountSynchronizedCommand;
import net.p2pexchangehub.core.handler.user.UserAccount;
import net.p2pexchangehub.core.handler.user.UserBankAccount;

@Singleton
public class ExternalBankAccountCommandHandler {

    @Inject
    private Logger log;

    @Inject
    @Any
    private Instance<BankProvider> bankProviders;
    
    @Inject
    private Repository<TestBankAccount> repositoryAccounts;

    @Inject
    private Repository<UserAccount> repositoryUserAccount;

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

    private Optional<BankProvider> getBankProvider(ExternalBankType bankType) {
        return StreamSupport.stream(bankProviders.spliterator(), false).filter(p -> p.getType()==bankType).findFirst();
    }
    
    @CommandHandler
    public void handle(RequestExternalBankTransactionCommand command) {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
                
        UserAccount userAccount = repositoryUserAccount.load(command.getUserAccountId());
        Optional<UserBankAccount> userBankAccount = userAccount.getBankAccount(command.getUserBankAccountId());
        
        //TODO: this is ugly shortcut, split into multiple event/command - UserAccountCreditExternalBankAccountManager or new one for external bank account
        // it would be handy to allow retires etc.
        
        Optional<BankProvider> bankProvider = getBankProvider(bankAccount.getBankType());
  
        TransactionRequestExternal transactionRequest = new TransactionRequestExternal();
        transactionRequest.setId(command.getTransactionId());
        transactionRequest.setBankAccount(bankAccount);
        transactionRequest.setAmount(command.getAmount().getAmount());
        transactionRequest.setDetailInfo(userAccount.getPaymentsCode());
        transactionRequest.setRecipientAccountNumber(userBankAccount.get().getAccountNumber());
          
        try {
            bankProvider.get().processTransactionRequest(transactionRequest);
            bankAccount.externalTransactionRequestSucceeded(command.getTransactionId(), command.getUserAccountId(), command.getUserBankAccountId(), command.getAmount());
        } catch (BankProviderException e) {
            bankAccount.externalTransactionRequestFailed(command.getTransactionId(), command.getUserAccountId(), command.getUserBankAccountId(), command.getAmount());
        }
        
    }
    
}
