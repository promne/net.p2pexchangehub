package es.command.handler;

import javax.inject.Inject;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;

import es.aggregate.ExchangeOffer;
import es.aggregate.ExternalBankAccount;
import es.aggregate.ExternalBankTransaction;
import es.aggregate.TestBankAccount;
import es.aggregate.TransactionSplit.PartnerType;
import es.aggregate.UserAccount;
import es.command.MatchExternalBankTransactionWithOfferCommand;
import es.command.MatchExternalBankTransactionWithUserAccountCommand;

public class ExternalBankTransactionCommandHandler {

    @Inject
    private Repository<ExternalBankTransaction> repositoryTransactions;

    @Inject
    private Repository<TestBankAccount> repositoryAccounts;

    @Inject
    private Repository<ExchangeOffer> repositoryOffer;

    @Inject
    private Repository<UserAccount> repositoryUserAccount;    
    
    @CommandHandler
    public void matchWithOffer(MatchExternalBankTransactionWithOfferCommand command) {
        ExternalBankTransaction bankTransaction = repositoryTransactions.load(command.getTransactionId());
        bankTransaction.matchWith(command.getOfferId(), PartnerType.OFFER, command.getAmount());
        
        ExchangeOffer exchangeOffer = repositoryOffer.load(command.getOfferId());
        exchangeOffer.matchIncomingTransaction(command.getTransactionId(), command.getAmount());        
    }
    
    @CommandHandler
    public void handleMatchTransaction(MatchExternalBankTransactionWithUserAccountCommand command) {
        ExternalBankTransaction bankTransaction = repositoryTransactions.load(command.getTransactionId());
        bankTransaction.matchWith(command.getUserAccountId(), PartnerType.USER_ACCOUNT, command.getAmount());

        ExternalBankAccount bankAccount = repositoryAccounts.load(bankTransaction.getBankAccountId());
        
        UserAccount userAccount = repositoryUserAccount.load(command.getUserAccountId());
        userAccount.matchIncomingTransaction(command.getTransactionId(), command.getAmount(), bankAccount.getCurrency());
    }
    
}
