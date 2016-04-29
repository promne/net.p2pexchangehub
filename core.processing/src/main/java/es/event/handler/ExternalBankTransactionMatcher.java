package es.event.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.EventReplayUnsupportedException;
import org.axonframework.eventhandling.replay.ReplayAware;

import es.command.MatchExternalBankTransactionWithOfferCommand;
import es.command.MatchExternalBankTransactionWithUserAccountCommand;
import es.event.ExternalBankTransactionCreatedEvent;
import esw.domain.BankAccount;
import esw.domain.Offer;
import esw.view.BankAccountView;
import esw.view.OfferView;
import george.test.exchange.core.domain.offer.OfferState;

@ApplicationScoped
public class ExternalBankTransactionMatcher implements ReplayAware {

    @Inject
    private OfferView offerView;
    
    @Inject
    private BankAccountView bankAccountView;
    
    @Inject
    CommandGateway gateway;
    
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
    public void handleCreated(ExternalBankTransactionCreatedEvent event) {
        if (event.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            matchWithOffer(event);
        }
    }
    
    private void matchWithOffer(ExternalBankTransactionCreatedEvent event) {
        BankAccount bankAccount = bankAccountView.getBankAccount(event.getBankAccountId());
        List<Offer> matchedOffers = offerView.listOffersWithState(OfferState.WAITING_FOR_PAYMENT).stream().filter(o -> event.getDetailInfo().contains(o.getReferenceId())).filter(o -> o.getCurrencyOffered().equals(bankAccount.getCurrency())).collect(Collectors.toList());
        if (matchedOffers.size()==1) {
            Offer offer = matchedOffers.get(0);
            BigDecimal amountForOffer = offer.getAmountOffered().subtract(offer.getAmountReceived()).min(event.getAmount());
            gateway.send(new MatchExternalBankTransactionWithOfferCommand(event.getId(), offer.getId(), amountForOffer));
            
            BigDecimal reminder = event.getAmount().subtract(amountForOffer);
            if (reminder.compareTo(BigDecimal.ZERO)>0) {
                gateway.send(new MatchExternalBankTransactionWithUserAccountCommand(event.getId(), offer.getUserAccountId(), reminder));
            }
        }
    }
    
}
