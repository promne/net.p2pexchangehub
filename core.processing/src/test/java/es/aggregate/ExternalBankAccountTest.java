package es.aggregate;

import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

import george.test.exchange.core.domain.ExternalBankType;
import net.p2pexchangehub.core.api.external.bank.CreateExternalBankAccountCommand;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankAccountCredentialsSetEvent;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountCredentialsCommand;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccount;
import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccountCommandHandler;

public class ExternalBankAccountTest {

    private FixtureConfiguration<ExternalBankAccount> fixture;
    
    @Before
    public void setUp() {
        fixture = Fixtures.newGivenWhenThenFixture(ExternalBankAccount.class);
        ExternalBankAccountCommandHandler annotatedCommandHandler = new ExternalBankAccountCommandHandler();
//        annotatedCommandHandler.setRepositoryAccounts(fixture.getRepository());
        fixture.registerAnnotatedCommandHandler(annotatedCommandHandler);
    }
    
    @Test
    public void testCreateBankAccount() {
         fixture.given()
             .when(new CreateExternalBankAccountCommand("bankAccountId", "currency", "accountNumber", ExternalBankType.TEST))
             .expectEvents(new ExternalBankAccountCreatedEvent("bankAccountId", "currency", "accountNumber", ExternalBankType.TEST));         
    }

    @Test
    public void testSetCredentials() {
        fixture.given(new ExternalBankAccountCreatedEvent("bankAccountId", "currency", "accountNumber", ExternalBankType.TEST))
        .when(new SetExternalBankAccountCredentialsCommand("bankAccountId", "username", "password"))
        .expectEvents(new ExternalBankAccountCredentialsSetEvent("bankAccountId", "username", "password"));                 
    }
    
}
