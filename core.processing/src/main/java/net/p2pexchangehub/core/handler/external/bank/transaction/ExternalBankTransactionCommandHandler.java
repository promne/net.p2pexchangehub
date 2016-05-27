package net.p2pexchangehub.core.handler.external.bank.transaction;

import javax.inject.Inject;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;

import net.p2pexchangehub.core.api.external.bank.transaction.CreateExternalBankTransactionCommand;
import net.p2pexchangehub.core.api.external.bank.transaction.MatchIncomingExternalBankTransactionWithUserAccountCommand;
import net.p2pexchangehub.core.api.external.bank.transaction.MatchOutgoingExternalBankTransactionWithRequestedCommand;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccount;
import net.p2pexchangehub.core.handler.external.bank.ExternalTransactionRequest;
import net.p2pexchangehub.core.handler.external.bank.TestBankAccount;
import net.p2pexchangehub.core.handler.user.UserAccount;
import net.p2pexchangehub.view.repository.BankTransactionRepository;

public class ExternalBankTransactionCommandHandler {

    @Inject
    private Repository<ExternalBankTransaction> aggregateRepositoryTransactions;

    @Inject
    private Repository<TestBankAccount> aggregateRepositoryAccounts;

    @Inject
    private Repository<UserAccount> aggregateRepositoryUserAccount;    
    
    @Inject
    private BankTransactionRepository bankTransactionRepository;
    
    @CommandHandler
    public void createExternalTransaction(CreateExternalBankTransactionCommand command) {
        //accept any command and avoid duplicates using predetermined transaction id
        String uniqueID = String.format("%s-%s-%s%s", command.getBankAccountId(), command.getDate().getTime(), command.getAmount(), command.getBankSpecificTransactionData().getHash());
        if (!bankTransactionRepository.exists(uniqueID)) {
            ExternalBankTransaction ntr = new ExternalBankTransaction(uniqueID, command.getBankAccountId(), command.getAmount(), command.getDate(), command.getFromAccount(), command.getReferenceInfo(), command.getBankSpecificTransactionData());
            aggregateRepositoryTransactions.add(ntr);            
        }
    }
    
    @CommandHandler
    public void handleMatchTransaction(MatchIncomingExternalBankTransactionWithUserAccountCommand command) {
        ExternalBankTransaction bankTransaction = aggregateRepositoryTransactions.load(command.getTransactionId());
        UserAccount userAccount = aggregateRepositoryUserAccount.load(command.getUserAccountId());

        //TODO split to multiple steps
        bankTransaction.matchWith(command.getUserAccountId());
        userAccount.matchIncomingTransaction(command.getTransactionId(), bankTransaction.getAmount());
    }

    @CommandHandler
    public void handleMatchOutgoingTransaction(MatchOutgoingExternalBankTransactionWithRequestedCommand command) {
        ExternalBankTransaction bankTransaction = aggregateRepositoryTransactions.load(command.getExternalTransactionId());
        ExternalBankAccount bankAccount = aggregateRepositoryAccounts.load(bankTransaction.getBankAccountId());

        //TODO: better matching?
        ExternalTransactionRequest pendingRequestMatch = null;
        for (ExternalTransactionRequest pendingRequest : bankAccount.getPendingRequests().values()) {
            if (bankTransaction.getAmount().negate().equals(pendingRequest.getAmount())) {
                UserAccount userAccount = aggregateRepositoryUserAccount.load(pendingRequest.getUserAccountId());
                if (bankTransaction.getReferenceInfo().toUpperCase().contains(userAccount.getPaymentsCode())) {
                    pendingRequestMatch = pendingRequest;
                    break;
                }
            }
        }
        if (pendingRequestMatch!=null) {
            bankAccount.externalTransactionRequestConfirmed(pendingRequestMatch.getTransactionId(), bankTransaction.getId());
            bankTransaction.matchWith(pendingRequestMatch.getUserAccountId());
        }
    }
    
}
