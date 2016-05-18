package es.event.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import es.command.MatchExternalBankTransactionWithOfferCommand;
import es.command.MatchExternalBankTransactionWithUserAccountCommand;
import es.event.ExternalBankTransactionCreatedEvent;
import esw.domain.BankAccount;
import esw.domain.Offer;
import esw.view.BankAccountView;
import esw.view.OfferView;
import george.test.exchange.core.domain.offer.OfferState;

public class ExternalBankTransactionMatcher extends AbstractIgnoreReplayEventHandler {

    @Inject
    private OfferView offerView;
    
    @Inject
    private BankAccountView bankAccountView;
    
    @Inject
    CommandGateway gateway;
    
    @EventHandler
    public void handleCreated(ExternalBankTransactionCreatedEvent event) {
        if (isLive()) {
            if (event.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                matchIncomingWithOffer(event);
            } else {
                matchPaymentRequestWithOffer(event);            
            }            
        }
    }
    
    private void matchPaymentRequestWithOffer(ExternalBankTransactionCreatedEvent event) {
        Optional<BankAccount> bankAccount = bankAccountView.getBankAccount(event.getBankAccountId());
        List<Offer> matchedOffers = offerView.listOffersWithState(OfferState.SEND_MONEY_REQUESTED).stream()
                .filter(o -> event.getBankAccountId().equals(o.getOutgoingPaymentBankAccountId()))
                .filter(o -> o.getCurrencyRequested().equals(bankAccount.get().getCurrency()))
                .filter(o -> event.getAmount().abs().compareTo(o.getAmountRequested())==0)
                .filter(o -> event.getReferenceInfo().contains(o.getReferenceId()))
                .collect(Collectors.toList());        
        if (matchedOffers.size()==1) {
            Offer offer = matchedOffers.get(0);
            gateway.send(new MatchExternalBankTransactionWithOfferCommand(event.getId(), offer.getId(), event.getAmount()));            
        }
    }

    private void matchIncomingWithOffer(ExternalBankTransactionCreatedEvent event) {
        Optional<BankAccount> bankAccount = bankAccountView.getBankAccount(event.getBankAccountId());
        List<Offer> matchedOffers = offerView.listOffersWithState(OfferState.WAITING_FOR_PAYMENT).stream()
                .filter(o -> event.getBankAccountId().equals(o.getIncomingPaymentBankAccountId()))
                .filter(o -> o.getCurrencyOffered().equals(bankAccount.get().getCurrency()))
                .filter(o -> event.getReferenceInfo().contains(o.getReferenceId()))
                .collect(Collectors.toList());
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
