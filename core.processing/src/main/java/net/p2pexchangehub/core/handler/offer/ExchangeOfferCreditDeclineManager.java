package net.p2pexchangehub.core.handler.offer;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.offer.ConfirmOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.OfferCreditDeclineReservedEvent;
import net.p2pexchangehub.core.api.user.CreditUserAccountFromDeclinedOfferCommand;
import net.p2pexchangehub.core.api.user.UserAccountCreditedFromDeclinedOfferEvent;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;

public class ExchangeOfferCreditDeclineManager extends AbstractIgnoreReplayEventHandler {

    @Inject
    private CommandGateway gateway;
    
    @EventHandler
    public void handleCreditDeclineReserved(OfferCreditDeclineReservedEvent event) {
        if (isLive()) {
            gateway.send(new CreditUserAccountFromDeclinedOfferCommand(event.getUserAccountId(), event.getTransactionId(), event.getOfferId()));
        }
    }

    @EventHandler
    public void handleCreditDeclineAccepted(UserAccountCreditedFromDeclinedOfferEvent event) {
        if (isLive()) {
            gateway.send(new ConfirmOfferCreditDeclineCommand(event.getOfferId(), event.getTransactionId()));
        }
    }
    
}
