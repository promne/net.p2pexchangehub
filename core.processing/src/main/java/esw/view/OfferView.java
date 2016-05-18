package esw.view;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import esw.domain.Offer;
import george.test.exchange.core.domain.offer.OfferState;

@Stateless
public class OfferView {

    @PersistenceContext
    private EntityManager em;
    
    public String generateUniqueReferenceId(String currency) {
        //TODO generate new with regard to existing
        String uid = UUID.randomUUID().toString();
        return uid.substring(0, uid.indexOf('-'));
    }

    public List<Offer> listOffersWithState(OfferState state) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Offer> cq = cb.createQuery(Offer.class);
        Root<Offer> rootEntry = cq.from(Offer.class);
        CriteriaQuery<Offer> all = cq.select(rootEntry);
        cq.where(cb.equal(rootEntry.get(Offer.PROPERTY_STATE), state));
        TypedQuery<Offer> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    public Offer get(String offerId) {
        return em.find(Offer.class, offerId);
    }

    public List<Offer> listOffersForUser(String userAccountId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Offer> cq = cb.createQuery(Offer.class);
        Root<Offer> rootEntry = cq.from(Offer.class);
        CriteriaQuery<Offer> all = cq.select(rootEntry);
        cq.where(cb.equal(rootEntry.get(Offer.PROPERTY_USER_ACCOUNT_ID), userAccountId));
        TypedQuery<Offer> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }
    
}
