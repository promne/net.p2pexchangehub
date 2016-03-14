package george.test.exchange.core.processing.processor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import george.test.exchange.core.domain.ExternalBankTransactionRequestState;
import george.test.exchange.core.domain.ExternalBankTransactionState;
import george.test.exchange.core.domain.TransactionState;
import george.test.exchange.core.domain.entity.ExchangeOffer;
import george.test.exchange.core.domain.entity.TransactionIncomingExternal;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankTransaction;
import george.test.exchange.core.domain.offer.OfferState;
import george.test.exchange.core.processing.service.ExchangeOfferService;
import george.test.exchange.core.processing.service.bank.BankService;
import george.test.exchange.core.processing.service.bank.event.MatchExternalTransactionWithOfferEvent;

@Singleton
public class ExchangeOfferProcessor {

    @Inject
    private Logger log;
    
    @Inject
    private BankService bankService;
    
    @Inject
    private ExchangeOfferService offerService;
    
    @PersistenceContext
    private EntityManager em;
    
    @Asynchronous
    public void matchBankTransaction(@Observes(during = TransactionPhase.AFTER_SUCCESS) MatchExternalTransactionWithOfferEvent matchEvent) {
        ExternalBankTransaction bankTransaction = em.find(ExternalBankTransaction.class, matchEvent.getBankTransaction().getId());
        log.debug("Matching bank transaction {} in account {}", bankTransaction.getId(), bankTransaction.getBankAccount().getId());
        
        if (BigDecimal.ZERO.compareTo(bankTransaction.getAmount()) > 0) {
            matchOutgoingPayment(bankTransaction);
        } else {
            matchIncomingPayment(bankTransaction);            
        }
        em.flush();
    }

    private void matchIncomingPayment( ExternalBankTransaction bankTransaction) {
        // incoming payment, try to match with payment request
        List<ExchangeOffer> matchedOffers = offerService.listWaitingOffers(bankTransaction.getBankAccount()).stream().filter(offer -> bankTransaction.matchesReferenceId(offer.getReferenceId())).collect(Collectors.toList());
        if (matchedOffers.size()>1) {
            log.warn("Too many matched offers for bank transaction {}", bankTransaction.getId());            
        }
        if (matchedOffers.size()==1) {
            //create transaction
            ExchangeOffer exchangeOffer = matchedOffers.get(0);
            TransactionIncomingExternal incomingTransaction = new TransactionIncomingExternal();
            
            log.info("Matching offer {} with incoming bank transaction {} as transaction {}", exchangeOffer.getId(), bankTransaction.getId(), incomingTransaction.getId());

            // send money to the offer
            BigDecimal amountReminderForOffer = bankTransaction.getAmount().min(exchangeOffer.getAmountOffered().subtract(exchangeOffer.getAmountReceived()));
            
            incomingTransaction.setAmount(amountReminderForOffer);
            incomingTransaction.setExchangeOffer(exchangeOffer);
            incomingTransaction.setExternalBankTransaction(bankTransaction);
            incomingTransaction.setOwner(exchangeOffer.getOwner());
            incomingTransaction.setState(TransactionState.MATCHED_WITH_OFFER_CLOSED);
            em.persist(incomingTransaction);
            
            //send reminder to the user
            if (amountReminderForOffer.compareTo(bankTransaction.getAmount())!=0) {
                BigDecimal amountForUser = bankTransaction.getAmount().subtract(amountReminderForOffer);
                TransactionIncomingExternal incomingUserTransaction = new TransactionIncomingExternal();
                incomingUserTransaction.setAmount(amountForUser);
                incomingUserTransaction.setExternalBankTransaction(bankTransaction);
                incomingUserTransaction.setOwner(exchangeOffer.getOwner());
                incomingUserTransaction.setState(TransactionState.MATCHED_WITH_USER_PENDING);
                log.info("Offer {} received surplus in incoming bank transaction {} sending it to user account in transaction {}", exchangeOffer.getId(), bankTransaction.getId(), incomingUserTransaction.getId());
                em.persist(incomingUserTransaction);
            }

            exchangeOffer.setAmountReceived(exchangeOffer.getAmountReceived().add(amountReminderForOffer));
            if (exchangeOffer.getAmountReceived().compareTo(exchangeOffer.getAmountOffered()) == 0) {
                log.info("Offer {} received offered amount", exchangeOffer.getId());
                exchangeOffer.setState(OfferState.PAYMENT_RECEIVED);
            }
            em.merge(exchangeOffer);                
            
            
            bankTransaction.setState(ExternalBankTransactionState.MATCHED);
            em.merge(bankTransaction);
        }
    }

    private void matchOutgoingPayment( ExternalBankTransaction bankTransaction) {
        // outgoing payment, try to match with payment request
        List<TransactionRequestExternal> matchedRequests = bankService.listTransactionRequests(ExternalBankTransactionRequestState.PENDING).stream().filter(bankTransaction::matches).collect(Collectors.toList());
        if (matchedRequests.size()>1) {
            log.warn("Too many matched transaction requests for bank transaction {}", bankTransaction.getId());
        }
        if (matchedRequests.size()==1) {
            TransactionRequestExternal externalBankTransactionRequest = matchedRequests.get(0);
            ExchangeOffer exchangeOffer = externalBankTransactionRequest.getExchangeOffer();
            log.info("Matching confirmation of outgoing bank transaction {} for the offer {}", bankTransaction.getId(), exchangeOffer.getId());
            
            if (exchangeOffer.getState() != OfferState.SEND_MONEY_REQUESTED) {
                throw new IllegalStateException(String.format("Unable to close offer %s with state %s", exchangeOffer.getId(), exchangeOffer.getState()));
            }
            
            externalBankTransactionRequest.setExternalBankTransaction(bankTransaction);
            externalBankTransactionRequest.setRequestState(ExternalBankTransactionRequestState.CLOSED);
            em.merge(externalBankTransactionRequest);

            log.info("Outgoing payment transaction confirmed, closing offer {}", exchangeOffer.getId());
            exchangeOffer.setState(OfferState.CLOSED);
            em.merge(exchangeOffer);
        }
    }
    
}
