package es.command;

import java.math.BigDecimal;
import java.util.Date;

public class SetExternalBankAccountSynchronizedCommand {

    private final String bankAccountId;
    
    private final Date syncDate;
    
    private final BigDecimal balance;

    public SetExternalBankAccountSynchronizedCommand(String bankAccountId, Date syncDate, BigDecimal balance) {
        super();
        this.bankAccountId = bankAccountId;
        this.syncDate = syncDate;
        this.balance = balance;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public Date getSyncDate() {
        return syncDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }        
    
}
