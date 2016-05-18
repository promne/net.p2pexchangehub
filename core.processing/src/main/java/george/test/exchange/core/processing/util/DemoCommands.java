package george.test.exchange.core.processing.util;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.CreateConfigurationItemCommand;
import es.command.CreateExternalBankAccountCommand;
import es.command.CreateUserAccountCommand;
import es.command.CreateUserBankAccountCommand;
import es.command.RequestExternalBankSynchronizationCommand;
import es.command.SetExternalBankAccountActiveCommand;
import es.command.SetExternalBankAccountCredentialsCommand;
import es.command.SetUserAccountPasswordCommand;
import esw.view.ConfigurationView;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.processing.scheduler.ExternalBankAccountImportScheduler;
import george.test.exchange.core.processing.service.bank.provider.test.TestBankProvider;

@Singleton
@Startup
@DependsOn({ConfigurationView.BEAN_NAME, "DemoData"})
public class DemoCommands {

    @Inject
    CommandGateway gateway;
    
    @PostConstruct
    public void init() {
//        initSE();
    }
    
    public void initSE() {

        gateway.send(new CreateConfigurationItemCommand(TestBankProvider.CONFIG_WS_URL, "http://localhost:8080/simple.bank.war/api/"));
        gateway.send(new CreateConfigurationItemCommand(ExternalBankAccountImportScheduler.CONFIG_BANK_IMPORT_INTERVAL_PREFIX, "45000"));
        gateway.send(new CreateConfigurationItemCommand(ExternalBankAccountImportScheduler.CONFIG_SCHEDULE_INTERVAL, "20000"));
        
        
        String acc00Id = "acc0-0";
        gateway.send(new CreateExternalBankAccountCommand(acc00Id, "CZK", "0-0", ExternalBankType.TEST));
        gateway.send(new SetExternalBankAccountCredentialsCommand(acc00Id, "username0", "pwd0"));
        gateway.send(new RequestExternalBankSynchronizationCommand(acc00Id));
        gateway.send(new SetExternalBankAccountActiveCommand(acc00Id, true));

        String acc01Id = "acc0-1";
        gateway.send(new CreateExternalBankAccountCommand(acc01Id, "NZD", "0-1", ExternalBankType.TEST));
        gateway.send(new SetExternalBankAccountCredentialsCommand(acc01Id, "username0", "pwd0"));
        gateway.send(new RequestExternalBankSynchronizationCommand(acc01Id));
        gateway.send(new SetExternalBankAccountActiveCommand(acc01Id, true));

        String acc10Id = "acc1-0";
        gateway.send(new CreateExternalBankAccountCommand(acc10Id, "NZD", "1-0", ExternalBankType.TEST));
        gateway.send(new SetExternalBankAccountCredentialsCommand(acc10Id, "username1", "pwd1"));
        gateway.send(new RequestExternalBankSynchronizationCommand(acc10Id));
        gateway.send(new SetExternalBankAccountActiveCommand(acc10Id, true));

        String acc11Id = "acc1-1";
        gateway.send(new CreateExternalBankAccountCommand(acc11Id, "CZK", "1-1", ExternalBankType.TEST));
        gateway.send(new SetExternalBankAccountCredentialsCommand(acc11Id, "username1", "pwd1"));
        gateway.send(new RequestExternalBankSynchronizationCommand(acc11Id));
        gateway.send(new SetExternalBankAccountActiveCommand(acc11Id, true));

        String userAccountId1 = "usac1";
        gateway.send(new CreateUserAccountCommand(userAccountId1 , "username1"));
        gateway.send(new SetUserAccountPasswordCommand(userAccountId1 , "password1"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "CZK", "2-0"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "NZD", "2-1"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "CZK", "2-2"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "NZD", "2-3"));

        String userAccountId2 = "usac2";
        gateway.send(new CreateUserAccountCommand(userAccountId2 , "usernameIsHere"));
        gateway.send(new SetUserAccountPasswordCommand(userAccountId2 , "password2"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId2, "CZK", "2-0"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId2, "NZD", "2-1"));
        
        
//        gateway.send(new CreateOfferCommand("offerId", userAccountId1, "CZK", BigDecimal.ZERO, BigDecimal.TEN, "NZD", new BigDecimal("0.1333")));
//        gateway.send(new SetOwnerAccountNumberForOfferCommand("offerId", "2-1"));
//        
//        gateway.send(new MatchExchangeOfferCommand("newOfferId", "offerId", userAccountId2, new BigDecimal(1.5), new BigDecimal(0.5)));
//        gateway.send(new SetOwnerAccountNumberForOfferCommand("newOfferId", "2-0"));
 
        
    }
    
}
