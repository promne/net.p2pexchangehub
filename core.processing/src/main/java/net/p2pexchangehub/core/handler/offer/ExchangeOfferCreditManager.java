package net.p2pexchangehub.core.handler.offer;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.offer.CreditOfferCommand;
import net.p2pexchangehub.core.api.offer.OfferCreditRejectedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditedEvent;
import net.p2pexchangehub.core.api.user.ConfirmAccountDebitReservationCommand;
import net.p2pexchangehub.core.api.user.DiscardAccountDebitReservationCommand;
import net.p2pexchangehub.core.api.user.UserAccountDebitForOfferReservedEvent;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;

public class ExchangeOfferCreditManager extends AbstractIgnoreReplayEventHandler {

    @Inject
    private CommandGateway gateway;
    
    @EventHandler
    public void handleAccountDebitReserved(UserAccountDebitForOfferReservedEvent event) {
        if (isLive()) {
            gateway.send(new CreditOfferCommand(event.getUserAccountId(), event.getTransactionId(), event.getOfferId(), event.getAmount()));
        }
    }

    @EventHandler
    public void handleOfferCredited(OfferCreditedEvent event) {
        if (isLive()) {
            gateway.send(new ConfirmAccountDebitReservationCommand(event.getUserAccountId(), event.getTransactionId()));
        }
    }

    @EventHandler
    public void handleOfferCreditRejected(OfferCreditRejectedEvent event) {
        if (isLive()) {
            gateway.send(new DiscardAccountDebitReservationCommand(event.getUserAccountId(), event.getTransactionId()));
        }
    }
    
}
