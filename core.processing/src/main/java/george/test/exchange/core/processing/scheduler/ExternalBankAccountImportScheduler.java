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

import es.command.SynchronizeExternalBankTransactionsCommand;
import esw.domain.BankAccount;
import esw.view.BankAccountView;
import esw.view.ConfigurationView;
import george.test.exchange.core.processing.service.bank.BankProvider;

@Singleton
@Startup
@DependsOn({"DemoCommands"})
public class ExternalBankAccountImportScheduler {

    public static final String CONFIG_SCHEDULE_INTERVAL = "bank.scheduler.import.interval";
    
    public static final String CONFIG_BANK_IMPORT_INTERVAL_PREFIX = BankProvider.CONFIG_BANK_PROVIDER_PREFIX + ".import.interval";
    
    @Resource
    private TimerService timerService;
    
    @Inject
    private BankAccountView accountsView;
    
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
        long duration = configurationView.getValueInt(CONFIG_SCHEDULE_INTERVAL);
        if (duration != timeoutValue) {
            timerService.getTimers().stream().forEach(Timer::cancel);
            timerService.createIntervalTimer(duration, duration, new TimerConfig(null, false));
            timeoutValue = duration;
        }
    }
    
    @Timeout
    public void listAccountStatements() {
        int defaultInterval = configurationView.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX);
        
        accountsView.listBankAccounts().stream().filter(BankAccount::isActive).forEach(bankAccount -> {
            int scrapeInterval = configurationView.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX + "." + bankAccount.getBankType(), defaultInterval);
            if (bankAccount.getLastCheck()==null || Instant.now().minusMillis(scrapeInterval).isAfter(bankAccount.getLastCheck().toInstant())) {
                gateway.send(new SynchronizeExternalBankTransactionsCommand(bankAccount.getId()));                
            }
        });
        
        reschedule();
    }
}
