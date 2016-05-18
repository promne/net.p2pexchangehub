package es.aggregate;

import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

import es.command.CreateExternalBankAccountCommand;
import es.command.SetExternalBankAccountCredentialsCommand;
import es.command.handler.ExternalBankAccountCommandHandler;
import es.event.ExternalBankAccountCreatedEvent;
import es.event.ExternalBankAccountCredentialsSetEvent;
import george.test.exchange.core.domain.ExternalBankType;

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
