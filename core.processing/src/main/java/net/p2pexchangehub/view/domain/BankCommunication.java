package net.p2pexchangehub.view.domain;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;

public class BankCommunication {

    @Id
    private String id = UUID.randomUUID().toString();
    
    private Date timestamp;

    private String bankAccountId;
    public static final String BANK_ACCOUNT_ID_PROPERTY = "bankAccountId";

    private String data;

    public BankCommunication() {
        super();
    }

    public BankCommunication(Date timestamp, String bankAccountId, String data) {
        super();
        this.timestamp = timestamp;
        this.bankAccountId = bankAccountId;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
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

    public Date getTimestamp() {
        return timestamp;
    }
        
}
