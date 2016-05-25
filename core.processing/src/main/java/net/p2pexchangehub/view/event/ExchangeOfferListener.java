package net.p2pexchangehub.view.event;

import javax.inject.Inject;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.annotation.Timestamp;
import org.axonframework.eventhandling.replay.ReplayAware;
import org.joda.time.DateTime;

import net.p2pexchangehub.core.api.offer.OfferCanceledEvent;
import net.p2pexchangehub.core.api.offer.OfferCreatedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditDeclineConfirmedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditDeclineReservedEvent;
import net.p2pexchangehub.core.api.offer.OfferCreditedEvent;
import net.p2pexchangehub.core.api.offer.OfferDebitConfirmedEvent;
import net.p2pexchangehub.core.api.offer.OfferDebitRequestedEvent;
import net.p2pexchangehub.core.api.offer.OfferExchangeCompletedEvent;
import net.p2pexchangehub.core.api.offer.OfferMatchedEvent;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.OfferRepository;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class ExchangeOfferListener implements ReplayAware {

    @Inject
    private OfferRepository repository;
    
    @Inject
    private UserAccountRepository userAccountRepository;
    
    @EventHandler
    public void offerCreated(OfferCreatedEvent event, @Timestamp DateTime eventTimestamp) {
        UserAccount userAccount = userAccountRepository.findOne(event.getUserAccountId());
        
        Offer offer = new Offer();
        offer.setAmountOfferedMax(event.getAmountOfferedMax());
        offer.setAmountOfferedMin(event.getAmountOfferedMin());
        offer.setCurrencyOffered(event.getCurrencyOffered());
        offer.setCurrencyRequested(event.getCurrencyRequested());
        offer.setId(event.getOfferId());
        offer.setAmountRequestedExchangeRateFormula(event.getRequestedExchangeRate());
        offer.setUserAccountId(event.getUserAccountId());
        offer.setReferenceId(userAccount.getPaymentsCode());
        offer.changeState(OfferState.UNPAIRED, eventTimestamp);
        repository.save(offer);
    }
    
    @EventHandler
    public void handleMatched(OfferMatchedEvent event, @Timestamp DateTime eventTimestamp) {
        Offer offer = repository.findOne(event.getOfferId());
        offer.setAmountOffered(event.getAmountOffered());
        offer.setAmountRequested(event.getAmountRequested());
        offer.setMatchedExchangeOfferId(event.getMatchedOfferId());
        offer.changeState(OfferState.WAITING_FOR_PAYMENT, eventTimestamp);
        repository.save(offer);
    }

    private void updateOfferState(String offerId, OfferState state, DateTime eventTimestamp) {
        Offer offer = repository.findOne(offerId);
        offer.changeState(state, eventTimestamp);
        repository.save(offer);        
    }
    
    @EventHandler
    public void handleCredited(OfferCreditedEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.PAYED, eventTimestamp);
    }
    
    @EventHandler
    public void handleCreditDeclineRequested(OfferCreditDeclineReservedEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.CREDIT_DECLINE_REQUESTED, eventTimestamp);
    }
    
    @EventHandler
    public void handleCreditDeclineConfirmed(OfferCreditDeclineConfirmedEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.WAITING_FOR_PAYMENT, eventTimestamp);
    }

    @EventHandler
    public void handleExchangeCompleted(OfferExchangeCompletedEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.EXCHANGE_COMPLETE, eventTimestamp);
    }
    
    @EventHandler
    public void handleDebitRequested(OfferDebitRequestedEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.DEBIT_REQUESTED, eventTimestamp);
    }

    @EventHandler
    public void handleDebitConfirmed(OfferDebitConfirmedEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.CLOSED, eventTimestamp);
    }
    
    @EventHandler
    public void handleCanceled(OfferCanceledEvent event, @Timestamp DateTime eventTimestamp) {
        updateOfferState(event.getOfferId(), OfferState.CANCELED, eventTimestamp);
    }

    
    @Override
    public void beforeReplay() {
        repository.deleteAll();
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
    
}
