package george.test.exchange.core.processing.scheduler;

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

import es.command.RequestExternalBankSynchronizationCommand;
import esw.domain.BankAccount;
import esw.view.BankAccountView;
import esw.view.ConfigurationView;
import esw.view.OfferView;
import george.test.exchange.core.domain.offer.OfferState;
import george.test.exchange.core.processing.service.bank.BankProvider;

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
    private BankAccountView accountsView;
    
    @Inject
    private OfferView offerView;
    
    @Inject
    private ConfigurationView configurationView;
    
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
        
        accountsView.listBankAccounts().stream().filter(BankAccount::isActive).forEach(bankAccount -> {
            int scrapeInterval = configurationView.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX + "." + bankAccount.getBankType(), defaultInterval);
            
            boolean doSynchonization = offerView.listOffersWithState(OfferState.WAITING_FOR_PAYMENT).stream().anyMatch(offer -> bankAccount.getId().equals(offer.getIncomingPaymentBankAccountId()));
            doSynchonization = doSynchonization || offerView.listOffersWithState(OfferState.SEND_MONEY_REQUESTED).stream().anyMatch(offer -> bankAccount.getId().equals(offer.getOutgoingPaymentBankAccountId()));
            
            doSynchonization = doSynchonization && (bankAccount.getLastCheck()==null || Instant.now().minusMillis(scrapeInterval).isAfter(bankAccount.getLastCheck().toInstant()));
            
            if (doSynchonization) {
                gateway.send(new RequestExternalBankSynchronizationCommand(bankAccount.getId()));                
            }
        });
        
        reschedule();
    }
}
