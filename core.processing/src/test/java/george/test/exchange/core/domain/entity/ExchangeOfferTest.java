package george.test.exchange.core.domain.entity;

import java.math.BigDecimal;

import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.OfferCreatedEvent;
import net.p2pexchangehub.core.handler.offer.ExchangeOffer;
import net.p2pexchangehub.core.handler.offer.ExchangeOfferCommandHandler;

public class ExchangeOfferTest {

    private FixtureConfiguration<ExchangeOffer> fixture;
    
    @Before
    public void setUp() {
        fixture = Fixtures.newGivenWhenThenFixture(ExchangeOffer.class);
        ExchangeOfferCommandHandler annotatedCommandHandler = new ExchangeOfferCommandHandler();
        annotatedCommandHandler.setRepository(fixture.getRepository());
        fixture.registerAnnotatedCommandHandler(annotatedCommandHandler);
    }
    
    @Test
    public void testCreateOffer() {
         fixture.given()
             .when(new CreateOfferCommand("offerId", "userAccountId", "currencyOffered", BigDecimal.ZERO, BigDecimal.TEN, "currencyRequested", "1"))
             .expectEvents(new OfferCreatedEvent("offerId", "userAccountId", "currencyOffered", BigDecimal.ZERO, BigDecimal.TEN, "currencyRequested", "1"));         
    }
    
}
