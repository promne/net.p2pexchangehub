package net.p2pexchangehub.core.scheduler;

import java.time.Instant;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import george.test.exchange.core.processing.service.bank.BankProvider;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankSynchronizationCommand;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.repository.BankAccountRepository;
import net.p2pexchangehub.view.repository.ConfigurationRepository;
import net.p2pexchangehub.view.repository.OfferRepository;

@Singleton
@Startup
@DependsOn({"DemoCommands"})
public class ExternalBankAccountImportScheduler {

    public static final String CONFIG_SCHEDULE_INTERVAL = "bank.scheduler.import.interval";
    public static final Integer CONFIG_SCHEDULE_INTERVAL_DEFAULT = 300_000;
    
    public static final String CONFIG_BANK_IMPORT_INTERVAL_PREFIX = BankProvider.CONFIG_BANK_PROVIDER_PREFIX + ".import.interval";
    public static final Integer CONFIG_BANK_IMPORT_INTERVAL_DEFAULT = 600_000;
    
    @Resource
    private TimerService timerService;
    
    @Inject
    private BankAccountRepository bankAccountsRepository;
    
    @Inject
    private OfferRepository offerView;
    
    @Inject
    private ConfigurationRepository configurationView;
    
    @Inject
    CommandGateway gateway;
    
    private long timeoutValue = 0;
    
    @PostConstruct
    public void initialize(){
        reschedule();
    }
    
    private void reschedule() {
        long duration = configurationView.getValueInt(CONFIG_SCHEDULE_INTERVAL, CONFIG_SCHEDULE_INTERVAL_DEFAULT);
        if (duration != timeoutValue) {
            timerService.getTimers().stream().forEach(Timer::cancel);
            timerService.createIntervalTimer(duration, duration, new TimerConfig(null, false));
            timeoutValue = duration;
        }
    }
    
    @Timeout
    public void listAccountStatements() {
        int defaultInterval = configurationView.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX, CONFIG_BANK_IMPORT_INTERVAL_DEFAULT);
        
        bankAccountsRepository.findAll().stream().filter(BankAccount::isActive).forEach(bankAccount -> {
            int scrapeInterval = configurationView.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX + "." + bankAccount.getBankType(), defaultInterval);

            boolean doSynchonization = false;
            
//            boolean doSynchonization = offerView.findByState(OfferState.WAITING_FOR_PAYMENT).stream().anyMatch(offer -> bankAccount.getCurrency().equals(offer.getIncomingPaymentBankAccountId()));
//            doSynchonization = doSynchonization || offerView.findByState(OfferState.SEND_MONEY_REQUESTED).stream().anyMatch(offer -> bankAccount.getId().equals(offer.getOutgoingPaymentBankAccountId()));

            //TODO create fitting repository method
            doSynchonization = doSynchonization || bankAccount.isSynchronizationEnabled();
            doSynchonization = doSynchonization && (bankAccount.getLastCheck()==null || Instant.now().minusMillis(scrapeInterval).isAfter(bankAccount.getLastCheck().toInstant()));
            
            if (doSynchonization) {
                gateway.send(new RequestExternalBankSynchronizationCommand(bankAccount.getId()));                
            }
        });
        
        reschedule();
    }
}
