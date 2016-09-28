package net.p2pexchangehub.core.processing.service.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import net.p2pexchangehub.core.domain.ExternalBankType;
import net.p2pexchangehub.core.domain.entity.TransactionRequestExternal;
import net.p2pexchangehub.core.domain.entity.bank.ExternalBankTransaction;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccount;

//has to be generics free, otherwise WELD @Any doesn't work
public interface BankProvider {

    public static final String CONFIG_BANK_PROVIDER_PREFIX = "bank.provider";
    
    public ExternalBankType getType();

    public List<ExternalBankTransaction> listTransactions(ExternalBankAccount bankAccount, Date fromDate, Date toDate) throws BankProviderException;

    public void processTransactionRequest(TransactionRequestExternal externalBankTransactionRequest) throws BankProviderException;
    
    public BigDecimal getBalance(ExternalBankAccount bankAccount) throws BankProviderException;
    
}
