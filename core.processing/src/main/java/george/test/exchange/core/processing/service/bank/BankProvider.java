package george.test.exchange.core.processing.service.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import es.aggregate.ExternalBankAccount;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;

//has to be generics free, otherwise WELD @Any doesn't work
public interface BankProvider {

    public static final String CONFIG_BANK_PROVIDER_PREFIX = "bank.provider";
    
    public ExternalBankType getType();

    public List<ExternalBankTransaction> listTransactions(ExternalBankAccount bankAccount, Date fromDate, Date toDate) throws BankProviderException;

    public void processTransactionRequest(TransactionRequestExternal externalBankTransactionRequest) throws BankProviderException;
    
    public BigDecimal getBalance(ExternalBankAccount bankAccount) throws BankProviderException;
    
}
