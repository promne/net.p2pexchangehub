package george.test.exchange.core.processing.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import george.test.exchange.core.domain.ExternalBankTransactionRequestState;
import george.test.exchange.core.domain.TransactionState;
import george.test.exchange.core.domain.entity.ExchangeOffer;
import george.test.exchange.core.domain.entity.ExchangeOfferMatchRequest;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;
import george.test.exchange.core.domain.offer.OfferState;

@Stateless
public class ExchangeOfferService {
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private Logger log;
    
    public List<ExchangeOfferMatchRequest> listMatchRequests() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExchangeOfferMatchRequest> cq = cb.createQuery(ExchangeOfferMatchRequest.class);
        Root<ExchangeOfferMatchRequest> rootEntry = cq.from(ExchangeOfferMatchRequest.class);
        CriteriaQuery<ExchangeOfferMatchRequest> all = cq.select(rootEntry);
        TypedQuery<ExchangeOfferMatchRequest> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    public List<ExchangeOffer> listAllOffers() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExchangeOffer> cq = cb.createQuery(ExchangeOffer.class);
        Root<ExchangeOffer> rootEntry = cq.from(ExchangeOffer.class);
        CriteriaQuery<ExchangeOffer> all = cq.select(rootEntry);
        TypedQuery<ExchangeOffer> allQuery = em.createQuery(all);
        return allQuery.getResultList();        
    }

    public List<ExchangeOffer> listWaitingOffers(ExternalBankAccount bankAccount) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExchangeOffer> cq = cb.createQuery(ExchangeOffer.class);
        Root<ExchangeOffer> rootEntry = cq.from(ExchangeOffer.class);
        cq.where(cb.and(cb.equal(rootEntry.get(ExchangeOffer.STATE), OfferState.WAITING_FOR_PAYMENT), cb.equal(rootEntry.get(ExchangeOffer.INCOMIMG_PAYMENT_BANK_ACCOUNT), bankAccount)));
        CriteriaQuery<ExchangeOffer> all = cq.select(rootEntry);
        TypedQuery<ExchangeOffer> allQuery = em.createQuery(all);
        return allQuery.getResultList();        
    }
    
    public List<ExchangeOffer> listPaymentReadyOffers() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExchangeOffer> cq = cb.createQuery(ExchangeOffer.class);
        Root<ExchangeOffer> rootEntry = cq.from(ExchangeOffer.class);
        cq.where(cb.and(cb.equal(rootEntry.get(ExchangeOffer.STATE), OfferState.PAYMENT_RECEIVED), cb.equal(rootEntry.get(ExchangeOffer.MATCHED_EXCHANGE_OFFER).get(ExchangeOffer.STATE), OfferState.PAYMENT_RECEIVED)));
        CriteriaQuery<ExchangeOffer> all = cq.select(rootEntry);
        TypedQuery<ExchangeOffer> allQuery = em.createQuery(all);
        return allQuery.getResultList();        
    }
    
    public void createPaymentRequest(ExchangeOffer exchangeOffer) {
        ExchangeOffer offer = em.contains(exchangeOffer) ? exchangeOffer : em.find(ExchangeOffer.class, exchangeOffer.getId());
        if (offer.getState() != OfferState.PAYMENT_RECEIVED) {
            throw new IllegalStateException(String.format("Unable to request pay offer %s with state %s", offer.getId(), offer.getState()));
        }
        
        offer.setState(OfferState.SEND_MONEY_REQUESTED);
        em.merge(offer);
        
        TransactionRequestExternal transactionRequest = new TransactionRequestExternal();
        transactionRequest.setAmount(offer.getAmountRequested());
        transactionRequest.setBankAccount(offer.getMatchedExchangeOffer().getIncomimgPaymentBankAccount());
        transactionRequest.setDetailInfo(offer.getReferenceId());
        transactionRequest.setExchangeOffer(offer);
        transactionRequest.setOwner(exchangeOffer.getOwner());
        transactionRequest.setRecipientAccountNumber(offer.getOwnerAccountNumber());
        transactionRequest.setRequestState(ExternalBankTransactionRequestState.NEW);
        transactionRequest.setState(TransactionState.MATCHED_WITH_OFFER_CLOSED);
        em.persist(transactionRequest);
        
        log.info("Requesting outgoing payment transaction {} for offer {}", transactionRequest.getId(), offer.getId());
    }
    
}
