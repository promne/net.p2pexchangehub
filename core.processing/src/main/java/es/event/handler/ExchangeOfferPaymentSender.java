package es.event.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.EventReplayUnsupportedException;
import org.axonframework.eventhandling.replay.ReplayAware;

import es.command.RequestOfferPaymentCommand;
import es.event.OfferStateChangedEvent;
import esw.domain.Offer;
import esw.view.OfferView;
import george.test.exchange.core.domain.offer.OfferState;

@ApplicationScoped
public class ExchangeOfferPaymentSender  implements ReplayAware {

    @Inject
    private OfferView offerView;
    
    @Inject
    private CommandGateway gateway;
    
    @Override
    public void beforeReplay() {
        throw new EventReplayUnsupportedException("Generates commands");
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }

    @EventHandler
    public void handleCreated(OfferStateChangedEvent event) {
        if (OfferState.PAYMENT_RECEIVED == event.getNewState()) {
            Offer offer = offerView.get(event.getOfferId());            
            gateway.send(new RequestOfferPaymentCommand(offer.getId()));
            gateway.send(new RequestOfferPaymentCommand(offer.getMatchedExchangeOfferId()));            
        }
    }
    
}
