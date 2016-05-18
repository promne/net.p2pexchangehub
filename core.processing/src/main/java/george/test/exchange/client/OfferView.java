package george.test.exchange.client;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.Item;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MWindow;

import es.command.CreateOfferCommand;
import es.command.MatchExchangeOfferCommand;
import esw.domain.Offer;
import esw.view.BankAccountView;
import george.test.exchange.client.form.OfferEditor;
import george.test.exchange.core.domain.offer.OfferState;

@CDIView(OffersView.VIEW_NAME)
public class OffersView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "OffersView";

    @PersistenceContext
    private EntityManager em;

    @Inject
    private BankAccountView bankAccountView;
    
    @Inject
    private CommandGateway gateway;
    
    private JPAContainer<Offer> offersContainer;

    private String userAccountId = "usac1"; //TODO variable
    
    @PostConstruct
    private void init() {
        setSizeFull();

        offersContainer = JPAContainerFactory.make(Offer.class, em);
        offersContainer.setReadOnly(true);
        
        
        GeneratedPropertyContainer gpcOffers = new GeneratedPropertyContainer(offersContainer);
        gpcOffers.addGeneratedProperty("action", new PropertyValueGenerator<String>() {

            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                return offersContainer.getItem(itemId).getEntity().getState()==OfferState.UNPAIRED ? "Match" : null;
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        
        Grid offerGrid= new Grid("Offers", gpcOffers);
        offerGrid.setSizeFull();
        offerGrid.getColumn("action").setRenderer(new ButtonRenderer(e -> {
            Offer offer = offersContainer.getItem(e.getItemId()).getEntity();
                        
            BigDecimal amountRequested = offer.getAmountOfferedMin();
            BigDecimal amountOffered = amountRequested.multiply(offer.getAmountRequestedExchangeRate()).setScale(1, RoundingMode.UP);
            
            gateway.send(new MatchExchangeOfferCommand(UUID.randomUUID().toString(), offer.getId(), "usac2", amountOffered, amountRequested));
        }));
        
        addComponent(offerGrid);
        setExpandRatio(offerGrid, 1.0f);

        MHorizontalLayout c = new MHorizontalLayout();
        addComponent(c);
        c.addComponent(new Button("Refresh", e -> refreshOffers()));
        c.addComponent(new Button("New", e -> createNew()));
    }

    private void createNew() {
        OfferEditor editor = new OfferEditor(bankAccountView.listAvailableCurrencies());
        Window window = new MWindow("New offer", editor).withModal(true);
        
        editor.setSavedHandler(offer -> {
            window.close();
            gateway.send(new CreateOfferCommand(UUID.randomUUID().toString(), userAccountId, offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getAmountRequestedExchangeRate()));
        });
        
        editor.setResetHandler(offer -> {
            window.close();            
        });
        
        editor.setEntity(new Offer());
        
        getUI().addWindow(window);
    }

    private void refreshOffers() {
        offersContainer.refresh();
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshOffers();
    }
    
}
