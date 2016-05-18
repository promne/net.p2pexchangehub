package george.test.exchange.core.domain.offer;

public enum OfferState {

    UNPAIRED,
    WAITING_FOR_PAYMENT,
    PAYMENT_RECEIVED,
    SEND_MONEY_REQUESTED, 
    CLOSED,
    CANCELED
    ;
    
}
