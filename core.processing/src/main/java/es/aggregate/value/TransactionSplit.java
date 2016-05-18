package es.aggregate.value;

import java.math.BigDecimal;

public class TransactionSplit {

    public enum PartnerType {
        OFFER,
        USER_ACCOUNT;
    }
    
    private final String id;
    
    private final BigDecimal amount;

    private final String partnerId;
     
    private final PartnerType partnerType;
    
    public TransactionSplit(String id, BigDecimal amount, String partnerId, PartnerType partnerType) {
        super();
        this.id = id;
        this.amount = amount;
        this.partnerId = partnerId;
        this.partnerType = partnerType;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public PartnerType getPartnerType() {
        return partnerType;
    }
        
}
