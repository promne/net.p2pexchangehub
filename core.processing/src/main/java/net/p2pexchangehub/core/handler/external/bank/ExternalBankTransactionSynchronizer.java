package net.p2pexchangehub.core.handler.external.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.repository.Repository;

import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.processing.service.bank.BankProvider;
import george.test.exchange.core.processing.service.bank.BankProviderException;
import george.test.exchange.core.processing.service.bank.provider.test.TestBankTransaction;
import net.p2pexchangehub.core.aggregate.value.BankSpecificTransactionData;
import net.p2pexchangehub.core.aggregate.value.TestBankTransactionData;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountSynchronizationRequestedEvent;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountSynchronizedCommand;
import net.p2pexchangehub.core.api.external.bank.transaction.CreateExternalBankTransactionCommand;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;

public class ExternalBankTransactionSynchronizer extends AbstractIgnoreReplayEventHandler {
    
    @Inject
    private Repository<TestBankAccount> repositoryAccounts;    

    @Inject
    @Any
    private Instance<BankProvider> bankProviders;
    
    @Inject
    private CommandGateway gateway;
    
    private Optional<BankProvider> getBankProvider(ExternalBankType bankType) {
        return StreamSupport.stream(bankProviders.spliterator(), false).filter(p -> p.getType()==bankType).findFirst();
    }
    
    @EventHandler
    public void handle(ExternalBankAccountSynchronizationRequestedEvent event) throws BankProviderException {
        if (!isLive()) {
            return;
        }
        
        ExternalBankAccount bankAccount = repositoryAccounts.load(event.getBankAccountId());

        BankProvider bankProvider = getBankProvider(bankAccount.getBankType()).get();

        Date checkFrom = bankAccount.getLastCheck();
        if (checkFrom==null) {
            checkFrom = new Date(0);
        } else {
        }
        Date checkTo = new Date();
        try {
            List<george.test.exchange.core.domain.entity.bank.ExternalBankTransaction> transactions = bankProvider.listTransactions(bankAccount, checkFrom, checkTo);
            BigDecimal balance = bankProvider.getBalance(bankAccount);            
            if (!transactions.isEmpty()) {
                for (george.test.exchange.core.domain.entity.bank.ExternalBankTransaction tr : transactions) {
                    
                    BankSpecificTransactionData bankSpecificTransactionData;
                    String referenceInfo;
                    if (TestBankTransaction.class.equals(tr.getClass())) {
                        TestBankTransaction tbtr = (TestBankTransaction) tr;
                        referenceInfo = tbtr.getTbDetail();
                        bankSpecificTransactionData = new TestBankTransactionData(tbtr.getTbId(), tbtr.getTbDetail());                    
                    } else {
                        throw new IllegalStateException("Unable to process transaction of type " + tr.getClass());
                    }
                    
                    gateway.send(new CreateExternalBankTransactionCommand(event.getBankAccountId(), tr.getAmount(), tr.getDate(), tr.getOtherAccount(), referenceInfo, bankSpecificTransactionData));
                }
            }
            gateway.send(new SetExternalBankAccountSynchronizedCommand(event.getBankAccountId(), checkTo, balance));
        } catch (BankProviderException e) {
            //TODO send info about error
        }        
    }

}
