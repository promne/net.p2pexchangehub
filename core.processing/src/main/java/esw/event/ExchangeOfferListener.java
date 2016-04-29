package esw.event;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.eventhandling.annotation.EventHandler;

import es.event.OfferCreatedEvent;
import es.event.OfferIncomingTransactionMatchedEvent;
import es.event.OfferMatchedEvent;
import es.event.OfferOwnerBankNumberChangedEvent;
import es.event.OfferStateChangedEvent;
import esw.domain.Offer;

@Stateless
public class ExchangeOfferListener {

    @PersistenceContext
    private EntityManager em;
    
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
    
}
