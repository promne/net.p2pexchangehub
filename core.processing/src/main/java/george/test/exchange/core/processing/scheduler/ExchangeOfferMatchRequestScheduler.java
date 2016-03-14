package george.test.exchange.core.processing.scheduler;

import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;

import george.test.exchange.core.domain.entity.ExchangeOfferMatchRequest;
import george.test.exchange.core.processing.service.ConfigurationService;
import george.test.exchange.core.processing.service.ExchangeOfferService;

@Singleton
@Startup
public class ExchangeOfferMatchRequestScheduler {

    public static final String CONFIG_SCHEDULE_INTERVAL = "scheduler.exchangeOfferMatch.interval";

    @Resource
    private TimerService timerService;
    
    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private ExchangeOfferService exchangeOfferService;

    @Inject 
    private Event<ExchangeOfferMatchRequest> matchExchangeOfferEvent;    
    
    @PostConstruct
    public void initialize(){
        JsonObject scheduleConfig = Json.createReader(new StringReader(configurationService.getValueString(CONFIG_SCHEDULE_INTERVAL))).readObject();
        ScheduleExpression expression = new ScheduleExpression();
        expression.second(scheduleConfig.getString("second")).minute(scheduleConfig.getString("minute")).hour(scheduleConfig.getString("hour"));
        log.info("Initializing {}", expression);
        timerService.createCalendarTimer(expression, new TimerConfig(null, false));
    }
    
    @Timeout
    public void matchExchangeOffers() {
        exchangeOfferService.listMatchRequests().stream().forEach(matchExchangeOfferEvent::fire);;
    }
    
}
