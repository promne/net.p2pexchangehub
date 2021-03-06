package net.p2pexchangehub.core.util;

import java.util.Arrays;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import net.p2pexchangehub.core.api.configuration.CreateConfigurationItemCommand;
import net.p2pexchangehub.core.api.external.bank.CreateExternalBankAccountCommand;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankSynchronizationCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountActiveCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountCredentialsCommand;
import net.p2pexchangehub.core.api.notification.CreateNotificationTemplateCommand;
import net.p2pexchangehub.core.api.notification.UpdateEmailTemplateTextCommand;
import net.p2pexchangehub.core.api.user.AddUserAccountRolesCommand;
import net.p2pexchangehub.core.api.user.CreateUserAccountCommand;
import net.p2pexchangehub.core.api.user.EnableUserAccountCommand;
import net.p2pexchangehub.core.api.user.SetUserAccountPasswordCommand;
import net.p2pexchangehub.core.api.user.bank.CreateUserBankAccountCommand;
import net.p2pexchangehub.core.domain.ExternalBankType;
import net.p2pexchangehub.core.domain.UserAccountRole;
import net.p2pexchangehub.core.handler.notification.NotificationSender;
import net.p2pexchangehub.core.processing.service.bank.provider.test.TestBankProvider;
import net.p2pexchangehub.core.scheduler.ExternalBankAccountImportScheduler;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@Singleton
@Startup
public class DemoCommands {

    @Inject
    CommandGateway gateway;
    
    @Inject
    UserAccountRepository userAccountRepository;
    
    @PostConstruct
    public void init() {
        if (userAccountRepository.count()==0) {
            initSE();
        }
    }
    
    public void initSE() {

        gateway.send(new CreateConfigurationItemCommand(TestBankProvider.CONFIG_WS_URL, "http://localhost:8080/simple.bank.war/api/"));
        gateway.send(new CreateConfigurationItemCommand(ExternalBankAccountImportScheduler.CONFIG_BANK_IMPORT_INTERVAL_PREFIX, "45000"));
        gateway.send(new CreateConfigurationItemCommand(ExternalBankAccountImportScheduler.CONFIG_SCHEDULE_INTERVAL, "200000"));

        gateway.send(new CreateConfigurationItemCommand(NotificationSender.CONFIG_SMTP_SERVER_PROPERTIES, "mail.smtp.host=192.168.56.101\nmail.smtp.port=1025"));
        gateway.send(new CreateConfigurationItemCommand(NotificationSender.CONFIG_EMAIL_SENDER_DEFAULT, "noreply@p2pexchangehub.net"));

        gateway.send(new CreateNotificationTemplateCommand("net.p2pexchangehub.core.api.user.contact.ContactDetailValidationRequestedEvent"));
        gateway.send(new UpdateEmailTemplateTextCommand(
                "net.p2pexchangehub.core.api.user.contact.ContactDetailValidationRequestedEvent",
                "EN",
                "Confirmation of email address",
                "<html>\n" + 
                        "<body>\n"+
                        "<p>Dear ${UserAccount.username}</p>\n"+
                        "<p>it's necessary to confirm your email address <b>${UserAccountContact.value}</b>. To do that you need to click on the following link to "+
                        "<a href=\"http://localhost:8080/core.processing-0.0.1-SNAPSHOT/client/#!EmailConfirmationView/${TEMPLATE_SOURCE_EVENT.validationCode}\">validate this email address</a>.</p>\n"+
                        "<p>This code expires ${TEMPLATE_SOURCE_EVENT.validationCodeExpiration?datetime}.</p>\n" +
                        "<p>Best Regards</p>\n" +
                        "</body>\n"+
                        "</html>"
                ));
        
        
        
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

        String userAccountA = "s";
        gateway.send(new CreateUserAccountCommand(userAccountA , "s"));
        gateway.send(new SetUserAccountPasswordCommand(userAccountA , "s"));
        gateway.send(new AddUserAccountRolesCommand(userAccountA, new HashSet<>(Arrays.asList(UserAccountRole.ADMIN))));
        gateway.send(new EnableUserAccountCommand(userAccountA));
        
        String userAccountId1 = "usac1";
        gateway.send(new CreateUserAccountCommand(userAccountId1 , "username1"));
        gateway.send(new SetUserAccountPasswordCommand(userAccountId1 , "password1"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "CZK", "123456/2400", "accountOwnerName"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "NZD", "00-1234-1231237-00", "accountOwnerName"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "CZK", "6-123456/2400", "accountOwnerName"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId1, "NZD", "00-1234-1231237-01", "accountOwnerName"));

        String userAccountId2 = "usac2";
        gateway.send(new CreateUserAccountCommand(userAccountId2 , "usernameIsHere"));
        gateway.send(new SetUserAccountPasswordCommand(userAccountId2 , "password2"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId2, "CZK", "240/6666", "accountOwnerName"));
        gateway.send(new CreateUserBankAccountCommand(userAccountId2, "NZD", "12-1234-1234567-00", "accountOwnerName"));
        
        
//        gateway.send(new CreateOfferCommand("offerId", userAccountId1, "CZK", BigDecimal.ZERO, BigDecimal.TEN, "NZD", new BigDecimal("0.1333")));
//        gateway.send(new SetOwnerAccountNumberForOfferCommand("offerId", "2-1"));
//        
//        gateway.send(new MatchExchangeOfferCommand("newOfferId", "offerId", userAccountId2, new BigDecimal(1.5), new BigDecimal(0.5)));
//        gateway.send(new SetOwnerAccountNumberForOfferCommand("newOfferId", "2-0"));
 
        
    }
    
}
