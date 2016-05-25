package net.p2pexchangehub.core.handler.offer;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.offer.CompleteOfferExchangeCommand;
import net.p2pexchangehub.core.api.offer.OfferCreditedEvent;
import net.p2pexchangehub.core.api.offer.OfferExchangeCompletedEvent;
import net.p2pexchangehub.core.api.offer.RequestOfferDebitCommand;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.repository.OfferRepository;

public class ExchangeOfferPaymentSender extends AbstractIgnoreReplayEventHandler {

    @Inject
    private OfferRepository offerView;
    
    @Inject
    private CommandGateway gateway;
    
    @EventHandler
    public void handleCreated(OfferCreditedEvent event) {
        if (isLive()) {
            Offer offer = offerView.findOne(event.getOfferId());
            //TODO: yuck, do locking
            gateway.send(new CompleteOfferExchangeCommand(offer.getId()));            
            gateway.send(new CompleteOfferExchangeCommand(offer.getMatchedExchangeOfferId()));            
        }
    }

    @EventHandler
    public void handleCreated(OfferExchangeCompletedEvent event) {
        if (isLive()) {
            gateway.send(new RequestOfferDebitCommand(event.getOfferId()));            
        }
    }
    
}
