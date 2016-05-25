package net.p2pexchangehub.core.handler.offer;

public enum OfferState {

    UNPAIRED,
    WAITING_FOR_PAYMENT,
    
    PAYED,
    CREDIT_DECLINE_REQUESTED,

    EXCHANGE_COMPLETE,
    
    DEBIT_REQUESTED, 
    CLOSED,
    CANCELED
    ;
    
}
