package es.command.handler;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;

import es.aggregate.ExchangeOffer;
import es.aggregate.ExternalBankAccount;
import es.aggregate.ExternalBankTransaction;
import es.aggregate.TestBankAccount;
import es.aggregate.UserAccount;
import es.aggregate.value.TransactionSplit.PartnerType;
import es.command.CreateExternalBankTransactionCommand;
import es.command.MatchExternalBankTransactionWithOfferCommand;
import es.command.MatchExternalBankTransactionWithUserAccountCommand;
import esw.domain.BankTransaction;

public class ExternalBankTransactionCommandHandler {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private Repository<ExternalBankTransaction> repositoryTransactions;

    @Inject
    private Repository<TestBankAccount> repositoryAccounts;

    @Inject
    private Repository<ExchangeOffer> repositoryOffer;

    @Inject
    private Repository<UserAccount> repositoryUserAccount;    
    
    @CommandHandler
    public void createExternalTransaction(CreateExternalBankTransactionCommand command) {
        String uniqueID = String.format("%s-%s-%s%s", command.getBankAccountId(), command.getDate().getTime(), command.getAmount(), command.getBankSpecificTransactionData().getHash());
        if (em.find(BankTransaction.class, uniqueID)==null) {
            ExternalBankTransaction ntr = new ExternalBankTransaction(uniqueID, command.getBankAccountId(), command.getAmount(), command.getDate(), command.getFromAccount(), command.getReferenceInfo(), command.getBankSpecificTransactionData());
            repositoryTransactions.add(ntr);            
        }
    }
    
    @CommandHandler
    public void matchWithOffer(MatchExternalBankTransactionWithOfferCommand command) {
        ExternalBankTransaction bankTransaction = repositoryTransactions.load(command.getTransactionId());
        bankTransaction.matchWith(command.getOfferId(), PartnerType.OFFER, command.getAmount());
        
        ExchangeOffer exchangeOffer = repositoryOffer.load(command.getOfferId());
        if (command.getAmount().compareTo(BigDecimal.ZERO)>0) {
            exchangeOffer.matchIncomingTransaction(command.getTransactionId(), command.getAmount());                    
        } else {
            exchangeOffer.matchOutgoingTransaction(command.getTransactionId(), command.getAmount().abs());                                
        }
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
