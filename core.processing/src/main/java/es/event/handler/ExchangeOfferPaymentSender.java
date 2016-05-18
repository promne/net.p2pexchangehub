package es.event.handler;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import es.command.RequestOfferPaymentCommand;
import es.event.OfferStateChangedEvent;
import esw.domain.Offer;
import esw.view.OfferView;
import george.test.exchange.core.domain.offer.OfferState;

public class ExchangeOfferPaymentSender extends AbstractIgnoreReplayEventHandler {

    @Inject
    private OfferView offerView;
    
    @Inject
    private CommandGateway gateway;
    
    @EventHandler
    public void handleCreated(OfferStateChangedEvent event) {
        if (isLive()) {
            if (OfferState.PAYMENT_RECEIVED == event.getNewState()) {
                Offer offer = offerView.get(event.getOfferId());            
                gateway.send(new RequestOfferPaymentCommand(offer.getId()));
                gateway.send(new RequestOfferPaymentCommand(offer.getMatchedExchangeOfferId()));            
            }            
        }
    }
    
}
