package net.p2pexchangehub.core.domain.entity.bank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public abstract class ExternalBankTransaction {

    private String id = UUID.randomUUID().toString();
    
    private BigDecimal amount;
    
    private Date date;
    
    private String otherAccount;
        
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public abstract boolean matchesReferenceId(String referenceId);
    
}
