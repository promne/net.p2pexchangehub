package es.command;

import java.math.BigDecimal;

public class MatchExternalBankTransactionWithOfferCommand {

    private final String transactionId;

    private final String offerId;
    
    private final BigDecimal amount; 

    public MatchExternalBankTransactionWithOfferCommand(String transactionId, String offerId, BigDecimal amount) {
        super();
        this.transactionId = transactionId;
        this.offerId = offerId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOfferId() {
        return offerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
