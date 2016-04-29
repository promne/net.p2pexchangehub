package george.test.exchange.core.processing.util;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.CreateConfigurationItemCommand;
import es.command.CreateExternalBankAccountCommand;
import es.command.CreateOfferCommand;
import es.command.CreateUserAccountCommand;
import es.command.MatchExchangeOfferCommand;
import es.command.SetExternalBankAccountActiveCommand;
import es.command.SetExternalBankAccountCredentialsCommand;
import es.command.SetOwnerAccountNumberForOfferCommand;
import es.command.SynchronizeExternalBankTransactionsCommand;
import esw.view.ConfigurationView;
import george.test.exchange.core.domain.ExternalBankType;
import george.test.exchange.core.processing.scheduler.ExternalBankAccountImportScheduler;
import george.test.exchange.core.processing.service.bank.provider.test.TestBankProvider;

@Singleton
@Startup
@DependsOn({ConfigurationView.BEAN_NAME})
public class DemoCommands {

    @Inject
    CommandGateway gateway;
    
    @PostConstruct
    public void initSE() {

        gateway.send(new CreateConfigurationItemCommand(TestBankProvider.CONFIG_WS_URL, "http://localhost:8080/simple.bank.war/api/"));
        gateway.send(new CreateConfigurationItemCommand(ExternalBankAccountImportScheduler.CONFIG_BANK_IMPORT_INTERVAL_PREFIX, "45000"));
        gateway.send(new CreateConfigurationItemCommand(ExternalBankAccountImportScheduler.CONFIG_SCHEDULE_INTERVAL, "20000"));
        
        
        String acc1Id = "acc1";
        gateway.send(new CreateExternalBankAccountCommand(acc1Id, "NZD", "country", "0-0", ExternalBankType.TEST));
        gateway.send(new SetExternalBankAccountCredentialsCommand(acc1Id, "username0", "pwd0"));
        gateway.send(new SynchronizeExternalBankTransactionsCommand(acc1Id));
        gateway.send(new SetExternalBankAccountActiveCommand(acc1Id, true));

        String acc2Id = "acc2";
        gateway.send(new CreateExternalBankAccountCommand(acc2Id, "CZK", "country2", "1-0", ExternalBankType.TEST));
        gateway.send(new SetExternalBankAccountCredentialsCommand(acc2Id, "username1", "pwd1"));
        gateway.send(new SynchronizeExternalBankTransactionsCommand(acc2Id));
        gateway.send(new SetExternalBankAccountActiveCommand(acc2Id, true));

        String userAccountId1 = "usac1";
        gateway.send(new CreateUserAccountCommand(userAccountId1 , "username"));

        String userAccountId2 = "usac2";
        gateway.send(new CreateUserAccountCommand(userAccountId2 , "usernameIsHere"));
        
        
        gateway.send(new CreateOfferCommand("offerId", userAccountId1, "CZK", BigDecimal.ZERO, BigDecimal.TEN, "NZD", new BigDecimal("0.1333")));
        gateway.send(new SetOwnerAccountNumberForOfferCommand("offerId", "2-0"));
        
        gateway.send(new MatchExchangeOfferCommand("newOfferId", "offerId", userAccountId2, new BigDecimal(1.5), new BigDecimal(0.5)));
        gateway.send(new SetOwnerAccountNumberForOfferCommand("newOfferId", "2-1"));
 
        
//        MatchExchangeOfferCommand matchCommand
    }
    
}
