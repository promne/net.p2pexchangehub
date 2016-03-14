package george.test.exchange.core.processing.scheduler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import george.test.exchange.core.processing.service.ConfigurationService;
import george.test.exchange.core.processing.service.ExchangeOfferService;

@Singleton
@Startup
public class ExchangeOfferPaymentScheduler {

    public static final String CONFIG_SCHEDULE_INTERVAL = "scheduler.exchangeOfferPayment.interval";

    @Resource
    private TimerService timerService;
    
    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private ExchangeOfferService exchangeOfferService;
    
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
    public void matchExchangeOffers() {
        exchangeOfferService.listPaymentReadyOffers().stream().forEach(exchangeOfferService::createPaymentRequest);;
        reschedule();
    }
    
}
