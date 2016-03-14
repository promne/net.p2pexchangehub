package george.test.exchange.core.processing.scheduler;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import george.test.exchange.core.domain.ExternalBankTransactionRequestState;
import george.test.exchange.core.domain.entity.TransactionRequestExternal;
import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;
import george.test.exchange.core.processing.service.ConfigurationService;
import george.test.exchange.core.processing.service.bank.BankProvider;
import george.test.exchange.core.processing.service.bank.BankService;
import george.test.exchange.core.processing.service.bank.event.ExternalBankAccountRequestEvent;

@Singleton
@Startup
public class ExternalBankAccountImportScheduler {

    public static final String CONFIG_SCHEDULE_INTERVAL = "bank.scheduler.import.interval";
    
    public static final String CONFIG_BANK_IMPORT_INTERVAL_PREFIX = BankProvider.CONFIG_BANK_PROVIDER_PREFIX + ".import.interval";
    
    @Resource
    private TimerService timerService;
    
    @Inject
    private BankService bankService;
    
    @Inject
    private ConfigurationService configurationService;
    
    @Inject 
    private Event<ExternalBankAccountRequestEvent> externalBankAccountEvent;    
    
    private long timeoutValue = 0;
    
    @PostConstruct
    public void initialize(){
        reschedule();
    }
    
    private void reschedule() {
        long duration = configurationService.getValueInt(CONFIG_SCHEDULE_INTERVAL);
        if (duration != timeoutValue) {
            timerService.getTimers().stream().forEach(Timer::cancel);
            timerService.createIntervalTimer(duration, duration, new TimerConfig(null, false));
        }
    }
    
    @Timeout
    public void listAccountStatements() {
        int defaultInterval = configurationService.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX);
        
        Map<ExternalBankAccount, List<TransactionRequestExternal>> externalTransactionRequests = bankService.listTransactionRequests(ExternalBankTransactionRequestState.NEW).stream().collect(Collectors.groupingBy(TransactionRequestExternal::getBankAccount));
        
        for (ExternalBankAccount bankAccount : bankService.listExternalBankAccounts()) {
            int scrapeInterval = configurationService.getValueInt(CONFIG_BANK_IMPORT_INTERVAL_PREFIX + "." + bankAccount.getBankType(), defaultInterval);
            if (Instant.now().minusMillis(scrapeInterval).isAfter(bankAccount.getLastCheck().toInstant())) {
                if (!externalTransactionRequests.containsKey(bankAccount)) {
                    externalTransactionRequests.put(bankAccount, Collections.EMPTY_LIST);
                }
            }
        }

        externalTransactionRequests.entrySet().stream().map(e -> new ExternalBankAccountRequestEvent(e.getKey(), e.getValue())).forEach(externalBankAccountEvent::fire);
        reschedule();
    }
}
