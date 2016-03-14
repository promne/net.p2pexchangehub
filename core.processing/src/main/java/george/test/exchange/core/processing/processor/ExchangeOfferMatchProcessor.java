package george.test.exchange.core.processing.processor;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import george.test.exchange.core.domain.entity.ExchangeOffer;
import george.test.exchange.core.domain.entity.ExchangeOfferMatchRequest;
import george.test.exchange.core.domain.offer.OfferState;
import george.test.exchange.core.processing.service.CurrencyService;
import george.test.exchange.core.processing.service.bank.BankService;

@Singleton
public class ExchangeOfferMatchProcessor {

    @Inject
    private Logger log;
    
    @Inject
    private BankService bankService;
    
    @Inject
    private CurrencyService currencyService;
    
    @PersistenceContext
    private EntityManager em;
    
    @Asynchronous
    public void matchRequest(@Observes ExchangeOfferMatchRequest offerMatchRequest) {
        ExchangeOffer exchangeOffer = em.find(ExchangeOffer.class, offerMatchRequest.getOffer().getId());
        if (OfferState.NEW == exchangeOffer.getState()) {
            
            BigDecimal acceptOfferAmountRequested = currencyService.calculateExchange(offerMatchRequest.getRequestedAmount(), exchangeOffer.getCurrencyOffered(), exchangeOffer.getCurrencyRequested(), exchangeOffer.getAmountRequestedExchangeRate());
            
            ExchangeOffer acceptOffer = new ExchangeOffer();
            acceptOffer.setAmountOffered(acceptOfferAmountRequested);
            acceptOffer.setAmountRequested(offerMatchRequest.getRequestedAmount());
            acceptOffer.setCreated(new Date());
            acceptOffer.setCurrencyOffered(exchangeOffer.getCurrencyRequested());
            acceptOffer.setCurrencyRequested(exchangeOffer.getCurrencyOffered());
            acceptOffer.setIncomimgPaymentBankAccount(bankService.getRandomBankAccount(acceptOffer.getCurrencyOffered()));
            acceptOffer.setMatchedExchangeOffer(exchangeOffer);
            acceptOffer.setOwner(offerMatchRequest.getUserAccountRequesting());
            acceptOffer.setOwnerAccountNumber(offerMatchRequest.getOwnerAccountNumber());
            acceptOffer.setReferenceId(acceptOffer.getId().substring(0, acceptOffer.getId().indexOf('-')));
            acceptOffer.setState(OfferState.WAITING_FOR_PAYMENT);
            
            exchangeOffer.setAmountOffered(offerMatchRequest.getRequestedAmount());
            exchangeOffer.setAmountRequested(acceptOfferAmountRequested);
            exchangeOffer.setIncomimgPaymentBankAccount(bankService.getRandomBankAccount(exchangeOffer.getCurrencyOffered()));
            exchangeOffer.setMatchedExchangeOffer(acceptOffer);
            exchangeOffer.setReferenceId(exchangeOffer.getId().substring(0, exchangeOffer.getId().indexOf('-')));
            exchangeOffer.setState(OfferState.WAITING_FOR_PAYMENT);
            
            log.info("Processing match request {} for offer {} creates offer {}", offerMatchRequest.getId(), exchangeOffer.getId(), acceptOffer.getId());
            
            em.persist(acceptOffer);
            em.merge(exchangeOffer);
        }
        
        ExchangeOfferMatchRequest matchRequest = em.find(ExchangeOfferMatchRequest.class, offerMatchRequest.getId());
        if (matchRequest != null) {
            em.remove(matchRequest);
        }
    }
    
}
