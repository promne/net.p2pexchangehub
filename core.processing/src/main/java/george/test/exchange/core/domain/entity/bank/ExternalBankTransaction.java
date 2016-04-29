package george.test.exchange.core.domain.entity.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Version;

import es.aggregate.ExternalBankAccount;
import george.test.exchange.core.domain.ExternalBankTransactionState;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ExternalBankTransaction {

    @Id
    private String id = UUID.randomUUID().toString();
    
    @Version
    private long version;
    
    private ExternalBankAccount bankAccount;
    public static final String BANK_ACCOUNT = "bankAccount";

    private BigDecimal amount;
    
    private Date date;
    public static final String DATE = "date";
    
    private ExternalBankTransactionState state;
    public static final String STATE = "state";

    public ExternalBankTransactionState getState() {
        return state;
    }

    public void setState(ExternalBankTransactionState status) {
        this.state = status;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((bankAccount == null) ? 0 : bankAccount.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExternalBankTransaction other = (ExternalBankTransaction) obj;
        if (amount == null) {
            if (other.amount != null) {
                return false;
            }
        } else if (!amount.equals(other.amount)) {
            return false;
        }
        if (bankAccount == null) {
            if (other.bankAccount != null) {
                return false;
            }
        } else if (!bankAccount.equals(other.bankAccount)) {
            return false;
        }
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        return true;
    }
    
    public boolean matches(TransactionRequestExternal request) {
        if (request.getAmount().compareTo(amount.negate())!=0) {
            return false;
        }
        if (!request.getBankAccount().getId().equals(bankAccount.getId())) {
            return false;
        }
        //TODO: match the date. Somehow. E.g. has to be later? wbu time precision ?
        return matchesReferenceId(request.getDetailInfo());
    }
    
    public abstract boolean matchesReferenceId(String referenceId);
}
