package george.test.exchange.core.processing.util;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import george.test.exchange.core.domain.entity.CurrencyConfiguration;

@Singleton
@Startup
public class DemoData {
    
    public static final String BEAN_NAME = "DemoData";

    @PersistenceContext
    private EntityManager em;
    
    @PostConstruct
    public void initSE() {
//        em.persist(new ConfigurationItem(TestBankProvider.CONFIG_WS_URL, "http://localhost:8080/simple.bank.war/api/"));
//        
//        em.persist(new ConfigurationItem(ExternalBankAccountImportScheduler.CONFIG_BANK_IMPORT_INTERVAL_PREFIX, "45000"));
//        em.persist(new ConfigurationItem(ExternalBankAccountImportScheduler.CONFIG_SCHEDULE_INTERVAL, "20000"));

        em.persist(new CurrencyConfiguration("CZK",1));
        em.persist(new CurrencyConfiguration("NZD",2));
      
    }
    
//    @PostConstruct
//    public void init() throws ParseException {
//        Date defaultLastCheck = new Date(0);
//        
//        TestBankAccount bankAccountTrustNZD = new TestBankAccount();
//        bankAccountTrustNZD.setAccountNumber("0-0");
//        bankAccountTrustNZD.setActive(true);
//        bankAccountTrustNZD.setUsername("username0");
//        bankAccountTrustNZD.setPassword("pwd0");
//        bankAccountTrustNZD.setLastCheck(defaultLastCheck);
//        bankAccountTrustNZD.setCurrency("NZD");
//        em.persist(bankAccountTrustNZD);
//
//        TestBankAccount bankAccountTrustCZK = new TestBankAccount();
//        bankAccountTrustCZK.setAccountNumber("0-1");
//        bankAccountTrustCZK.setActive(true);
//        bankAccountTrustCZK.setUsername("username0");
//        bankAccountTrustCZK.setPassword("pwd0");
//        bankAccountTrustCZK.setLastCheck(defaultLastCheck);
//        bankAccountTrustCZK.setCurrency("CZK");
//        em.persist(bankAccountTrustCZK);
//        
//        em.persist(new ConfigurationItem(TestBankProvider.CONFIG_WS_URL, "http://localhost:8080/simple.bank.war/api/"));
//        em.persist(new ConfigurationItem(ExternalBankAccountImportScheduler.CONFIG_BANK_IMPORT_INTERVAL_PREFIX, "45000"));
//        em.persist(new ConfigurationItem(ExternalBankAccountImportScheduler.CONFIG_SCHEDULE_INTERVAL, "20000"));
//        
//        em.persist(new ConfigurationItem(ExchangeOfferMatchRequestScheduler.CONFIG_SCHEDULE_INTERVAL, "{\"hour\":\"*\",\"minute\":\"*\",\"second\":\"*/10\"}"));
//        em.persist(new ConfigurationItem(ExchangeOfferPaymentScheduler.CONFIG_SCHEDULE_INTERVAL, "10000"));
//        
//        
//        em.persist(new CurrencyConfiguration("CZK",1));
//        em.persist(new CurrencyConfiguration("NZD",2));
//        
//        
//        // a ted setup uzivatele
//        UserAccount userAccount1 = new UserAccount();
//        userAccount1.setUsername("username");
//        userAccount1.setRoles(Arrays.asList(UserRole.values()));
//        em.persist(userAccount1);
//        
//        UserAccount userAccount2 = new UserAccount();
//        userAccount2.setUsername("username2");
//        em.persist(userAccount2);
//        
//        ExchangeOffer offer1 = new ExchangeOffer();
//        offer1.setAmountOfferedMin(new BigDecimal("10"));
//        offer1.setAmountOfferedMax(new BigDecimal("20"));
//        offer1.setAmountRequestedExchangeRate(new BigDecimal("16.1234"));
//        offer1.setCreated(new Date());
//        offer1.setCurrencyOffered("NZD");
//        offer1.setCurrencyRequested("CZK");
//        offer1.setOwner(userAccount1);
//        offer1.setOwnerAccountNumber("1-1");
//        offer1.setState(OfferState.NEW);
//        em.persist(offer1);
//        
//        ExchangeOfferMatchRequest offerMatchRequest = new ExchangeOfferMatchRequest();
//        offerMatchRequest.setOffer(offer1);
//        offerMatchRequest.setRequestedAmount(new BigDecimal("15.41"));
//        offerMatchRequest.setOwnerAccountNumber("2-0");
//        offerMatchRequest.setUserAccountRequesting(userAccount2);
//        em.persist(offerMatchRequest);
//     
//        UserAccountCreatedEvent createUserAccount = new UserAccountCreatedEvent();
//        createUserAccount.setUserId(UUID.randomUUID().toString());
//        createUserAccount.setUsername("username1");
//        em.persist(new EventStreamItem(createUserAccount));
//        
//        OfferCreatedEvent createOfferEvent = new OfferCreatedEvent();
//        createOfferEvent.setOfferId(UUID.randomUUID().toString());
//        createOfferEvent.setAmountOfferedMin(new BigDecimal("10"));
//        createOfferEvent.setAmountOfferedMax(new BigDecimal("20"));
//        createOfferEvent.setCurrencyOffered("CZK");
//        createOfferEvent.setCurrencyRequested("NZD");
//        createOfferEvent.setRequestedExchangeRate(new BigDecimal("0.06202"));
//        createOfferEvent.setUserAccountId(createUserAccount.getUserId());
//        em.persist(new EventStreamItem(createOfferEvent));
//        
//    }
    
}
