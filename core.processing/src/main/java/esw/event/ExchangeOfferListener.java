package esw.event;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import es.event.OfferCreatedEvent;
import es.event.OfferIncomingPaymentExternalAccountChangedEvent;
import es.event.OfferIncomingTransactionMatchedEvent;
import es.event.OfferMatchedEvent;
import es.event.OfferOutgoingTransactionMatchedEvent;
import es.event.OfferOwnerBankNumberChangedEvent;
import es.event.OfferStateChangedEvent;
import esw.domain.Offer;
import george.test.exchange.core.processing.util.JPAUtilsBean;

@Transactional
public class ExchangeOfferListener implements ReplayAware {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private JPAUtilsBean jpaUtils;
    
    @EventHandler
    public void offerCreated(OfferCreatedEvent event) {
        Offer offer = new Offer();
        
        offer.setAmountOfferedMax(event.getAmountOfferedMax());
        offer.setAmountOfferedMin(event.getAmountOfferedMin());
        offer.setCurrencyOffered(event.getCurrencyOffered());
        offer.setCurrencyRequested(event.getCurrencyRequested());
        offer.setId(event.getOfferId());
        offer.setAmountRequestedExchangeRate(event.getRequestedExchangeRate());
        offer.setUserAccountId(event.getUserAccountId());
        
        em.persist(offer);
    }
    
    @EventHandler
    public void handleStateChanged(OfferStateChangedEvent event) {
        Offer offer = em.find(Offer.class, event.getOfferId());
        offer.setState(event.getNewState());
        em.merge(offer);
    }

    @EventHandler
    public void handleOfferOwnerBankNumberChanged(OfferOwnerBankNumberChangedEvent event) {
        Offer offer = em.find(Offer.class, event.getOfferId());
        offer.setOwnerAccountNumber(event.getNewAccountNumber());
        em.merge(offer);
    }
    
    @EventHandler
    public void handleMatched(OfferMatchedEvent event) {
        Offer offer = em.find(Offer.class, event.getOfferId());
        offer.setAmountOffered(event.getAmountOffered());
        offer.setAmountRequested(event.getAmountRequested());
        offer.setMatchedExchangeOfferId(event.getMatchedOfferId());
        offer.setReferenceId(event.getReferenceId());
        em.merge(offer);
    }
 
    @EventHandler
    public void handleIncomingTransactionMatched(OfferIncomingTransactionMatchedEvent event) {
        Offer offer = em.find(Offer.class, event.getOfferId());
        offer.setAmountReceived(event.getNewBalance());
        em.merge(offer);
    }

    @EventHandler
    public void handleOutgoingTransactionMatched(OfferOutgoingTransactionMatchedEvent event) {
        Offer offer = em.find(Offer.class, event.getOfferId());
        offer.setAmountSent(event.getNewBalance());
        em.merge(offer);
    }

    @EventHandler
    public void handleOfferIncomingPaymentExternalAccountChanged(OfferIncomingPaymentExternalAccountChangedEvent event) {
        Offer offer = em.find(Offer.class, event.getOfferId());
        offer.setIncomingPaymentBankAccountId(event.getBankAccountId());
        
        Offer matchedOffer = em.find(Offer.class, offer.getMatchedExchangeOfferId());
        matchedOffer.setOutgoingPaymentBankAccountId(event.getBankAccountId());
        
        em.merge(offer);
        em.merge(matchedOffer);
    }

    @Override
    public void beforeReplay() {
        jpaUtils.deleteAll(Offer.class);
    }

    @Override
    public void afterReplay() {
    }

    @Override
    public void onReplayFailed(Throwable cause) {
    }
}
