package net.p2pexchangehub.core.processing.service.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.slf4j.Logger;

import net.p2pexchangehub.core.domain.entity.TransactionRequestExternal;
import net.p2pexchangehub.core.domain.entity.bank.ExternalBankTransaction;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccount;

// because of weld being unable to inject generic, do the mapping to right type
public abstract class BankProviderBase<A extends ExternalBankAccount, T extends ExternalBankTransaction, C extends BankProviderContext<A>> implements BankProvider {
    
    @Inject
    private Logger log;
    
    private static final Map<Object, BankProviderContext<?>> contextMap = new ConcurrentHashMap<>();
    
    public BankProviderBase() {
        super();
    }

    private C getContext(ExternalBankAccount bankAccount) throws BankProviderException {
        C context;
        synchronized(bankAccount.getClass()) {
            context = (C) contextMap.get(bankAccount.getId());
            if (context==null || !context.isActive()) {
                context = loginInternal((A)bankAccount);
                contextMap.put(bankAccount.getId(), context);
                log.info("Created new context {}", context);
            }
            context.setBankAccount((A)bankAccount);
        }
        return context;
    }
    protected abstract C loginInternal(A bankAccount) throws BankProviderException;    
    
    @Override
    public BigDecimal getBalance(ExternalBankAccount bankAccount) throws BankProviderException {
        C context = getContext(bankAccount);
        BigDecimal balanceInternal = getBalanceInternal(context);
        context.touch();
        return balanceInternal;
    }
    protected abstract BigDecimal getBalanceInternal(C context) throws BankProviderException;

    @Override
    public List<ExternalBankTransaction> listTransactions(ExternalBankAccount bankAccount, Date fromDate, Date toDate) throws BankProviderException {
        C context = getContext(bankAccount);
        log.info("Synchronizing transactions in session {}", context);
        List<T> listTransactionsInternal = listTransactionsInternal(context, fromDate, toDate);
        context.touch();
        return (List<ExternalBankTransaction>) listTransactionsInternal;
    }
    protected abstract List<T> listTransactionsInternal(C context, Date fromDate, Date toDate) throws BankProviderException;

    @Override
    public void processTransactionRequest(TransactionRequestExternal externalBankTransactionRequest) throws BankProviderException {
        C context = getContext(externalBankTransactionRequest.getBankAccount());
        log.info("Processing transaction request {} in session {}", externalBankTransactionRequest.getId(), context);
        processTransactionRequestInternal(context, externalBankTransactionRequest);
        context.touch();
    }
    protected abstract void processTransactionRequestInternal(C context, TransactionRequestExternal transactionRequest) throws BankProviderException;

}
