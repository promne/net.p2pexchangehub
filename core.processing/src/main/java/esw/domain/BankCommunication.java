package esw.domain;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class BankCommunication {

    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(columnDefinition="TIMESTAMP(6)")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @ManyToOne
    private BankAccount bankAccount;
    public static final String BANK_ACCOUNT_PROPERTY = "bankAccount";

//    @Lob removed because mongo ogm 
    private String data;

    public BankCommunication() {
        super();
    }

    public BankCommunication(Date timestamp, BankAccount bankAccount, String data) {
        super();
        this.timestamp = timestamp;
        this.bankAccount = bankAccount;
        this.data = data;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
        
}
