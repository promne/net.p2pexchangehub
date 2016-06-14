package net.p2pexchangehub.core.handler.user;

import java.util.Comparator;
import java.util.Optional;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.offer.OfferMatchedEvent;
import net.p2pexchangehub.core.api.user.CreditOfferFromUserAccountCommand;
import net.p2pexchangehub.core.api.user.UserIncomingTransactionMatchedEvent;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.repository.OfferRepository;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class UserAccountAutomaticOfferCharger {

    @Inject
    private UserAccountRepository userAccountRepository;

    @Inject
    private OfferRepository offerRepository;
    
    @Inject
    private CommandGateway commandGateway;
    
    @EventHandler
    public void incomingPayment(UserIncomingTransactionMatchedEvent event) {
        Optional<Offer> oOffer = offerRepository.findByUserAccountIdAndState(event.getUserAccountId(), OfferState.WAITING_FOR_PAYMENT).stream()                
            .filter(o -> o.getCurrencyOffered().equals(event.getAmount().getCurrencyCode()))
            .filter(o -> o.getAmountOffered().compareTo(event.getNewBalance().getAmount())<=0)
            .sorted(Comparator.comparing(Offer::getDateCreated))
            .findFirst();
        if (oOffer.isPresent()) {
            commandGateway.send(new CreditOfferFromUserAccountCommand(oOffer.get().getId()));
        }
    }
    
    @EventHandler
    public void offerMatched(OfferMatchedEvent event) {
        commandGateway.send(new CreditOfferFromUserAccountCommand(event.getOfferId()));
    }
    
}
