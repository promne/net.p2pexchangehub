package esw.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import george.test.exchange.core.domain.ExternalBankTransactionState;

@Entity
public class BankTransaction {

    @Id
    private String id;

    private BigDecimal amount;
    public static final String PROPERTY_AMOUNT = "amount";

    private Date date;
    public static final String PROPERTY_DATE = "date";

    @ManyToOne
    private BankAccount bankAccount;
    public static final String PROPERTY_BANK_ACCOUNT = "bankAccount";
    
    private ExternalBankTransactionState state;
    public static final String PROPERTY_STATE = "state";

    private String detail;
    public static final String PROPERTY_DETAIL = "detail";

    public BankTransaction() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ExternalBankTransactionState getState() {
        return state;
    }

    public void setState(ExternalBankTransactionState state) {
        this.state = state;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

}
