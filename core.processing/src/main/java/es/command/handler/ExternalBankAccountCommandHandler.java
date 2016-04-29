package es.command.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;

import es.aggregate.ExternalBankAccount;
import es.aggregate.ExternalBankTransaction;
import es.aggregate.TestBankAccount;
import es.command.CreateExternalBankAccountCommand;
import es.command.SetExternalBankAccountActiveCommand;
import es.command.SetExternalBankAccountCredentialsCommand;
import es.command.SynchronizeExternalBankTransactionsCommand;
import esw.domain.BankTransaction;
import esw.view.BankTransactionView;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.processing.service.bank.BankProvider;
import george.test.exchange.core.processing.service.bank.BankProviderException;
import george.test.exchange.core.processing.service.bank.provider.test.TestBankTransaction;

@Singleton
public class ExternalBankAccountCommandHandler {

    @Inject
    private Logger log;
    
    @Inject
    private Repository<TestBankAccount> repositoryAccounts;

    @Inject
    private Repository<ExternalBankTransaction> repositoryTransactions;
    
    @Inject
    @Any
    private Instance<BankProvider> bankProviders;
    
    @Inject
    private BankTransactionView bankTransactionView;
    
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
                account = new TestBankAccount(command.getBankAccountId(), command.getCurrency(), command.getCountry(), command.getAccountNumber());
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

    private Optional<BankProvider> getBankProvider(ExternalBankType bankType) {
        return StreamSupport.stream(bankProviders.spliterator(), false).filter(p -> p.getType()==bankType).findFirst();
    }
    
    @CommandHandler
    public void handleSynchronizeTransactions(SynchronizeExternalBankTransactionsCommand command) throws BankProviderException {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());

        BankProvider bankProvider = getBankProvider(bankAccount.getBankType()).get();

        Date checkFrom = bankAccount.getLastCheck();
        if (checkFrom==null) {
            checkFrom = new Date(0);
        }
        Date checkTo = new Date();
        List<george.test.exchange.core.domain.entity.bank.ExternalBankTransaction> transactions = bankProvider.listTransactions(bankAccount, checkFrom, checkTo);
        BigDecimal balance = bankProvider.getBalance(bankAccount);
        
        if (!transactions.isEmpty()) {
            List<ExternalBankTransaction> existingTransactions = bankTransactionView.listBankTransactions(command.getBankAccountId()).stream().map(BankTransaction::getId).map(repositoryTransactions::load).collect(Collectors.toList());
            
            //TODO better matching
            List<george.test.exchange.core.domain.entity.bank.ExternalBankTransaction> reallyNewOnes = transactions.stream().filter(tr -> !existingTransactions.stream().anyMatch(ex -> ex.getAmount().equals(tr.getAmount()) && ex.getDate().equals(tr.getDate()))).collect(Collectors.toList());
            
            for (george.test.exchange.core.domain.entity.bank.ExternalBankTransaction tr : reallyNewOnes) {
                TestBankTransaction tbtr = (TestBankTransaction) tr;
                ExternalBankTransaction ntr = new ExternalBankTransaction(UUID.randomUUID().toString(), command.getBankAccountId(), tbtr.getAmount(), tbtr.getDate(), tbtr.getTbFromAccount(), tbtr.getTbFromAccount() + "//" + tbtr.getTbDetail(), tbtr.getId());
                repositoryTransactions.add(ntr);
            }
        }
        bankAccount.setSynchronized(checkTo, balance);
    }
    
    @CommandHandler
    public void handleSetActive(SetExternalBankAccountActiveCommand command) {
        ExternalBankAccount bankAccount = repositoryAccounts.load(command.getBankAccountId());
        bankAccount.setActive(command.isActive());
    }

}
