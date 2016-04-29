package es.event;

import java.math.BigDecimal;

public class OfferIncomingTransactionMatchedEvent {

    private final String offerId;
    
    private final String transactionId;
    
    private final BigDecimal amount;

    private final BigDecimal newBalance;

    public OfferIncomingTransactionMatchedEvent(String offerId, String transactionId, BigDecimal amount, BigDecimal newBalance) {
        super();
        this.offerId = offerId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }
    
}
