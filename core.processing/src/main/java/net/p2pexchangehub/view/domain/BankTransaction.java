package net.p2pexchangehub.view.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import george.test.exchange.core.domain.ExternalBankTransactionState;

public class BankTransaction {

    @Id
    private String id;

    private BigDecimal amount;
    public static final String PROPERTY_AMOUNT = "amount";

    private Date date;
    public static final String PROPERTY_DATE = "date";

    @DBRef
    private BankAccount bankAccount;
    public static final String PROPERTY_BANK_ACCOUNT = "bankAccount";

    private String fromAccount;
    public static final String PROPERTY_FROM_ACCOUNT = "fromAccount";
    
    private ExternalBankTransactionState state;
    public static final String PROPERTY_STATE = "state";

    private String referenceInfo;
    public static final String PROPERTY_REFERENCE_INFO = "referenceInfo";
    
    private String detail;
    public static final String PROPERTY_DETAIL = "detail";

    public BankTransaction() {
        super();
    }

    public String getId() {
        return id;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIncoming() {
        return BigDecimal.ZERO.compareTo(amount) < 0;
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

    public String getReferenceInfo() {
        return referenceInfo;
    }

    public void setReferenceInfo(String referenceInfo) {
        this.referenceInfo = referenceInfo;
    }

}
