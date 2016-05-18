package george.test.exchange.core.domain.entity.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import es.aggregate.ExternalBankAccount;
import george.test.exchange.core.domain.ExternalBankTransactionState;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ExternalBankTransaction {

    @Id
    private String id = UUID.randomUUID().toString();
    
    private ExternalBankAccount bankAccount;
    public static final String BANK_ACCOUNT = "bankAccount";

    private BigDecimal amount;
    
    private Date date;
    public static final String DATE = "date";
    
    private ExternalBankTransactionState state;
    public static final String STATE = "state";

    private String otherAccount;
        
    public ExternalBankTransactionState getState() {
        return state;
    }

    public void setState(ExternalBankTransactionState status) {
        this.state = status;
    }

    public String getOtherAccount() {
        return otherAccount;
    }

    public void setOtherAccount(String otherAccount) {
        this.otherAccount = otherAccount;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExternalBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(ExternalBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public abstract boolean matchesReferenceId(String referenceId);
    
}
