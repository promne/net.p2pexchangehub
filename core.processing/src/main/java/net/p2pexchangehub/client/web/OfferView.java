package net.p2pexchangehub.client.web;

import com.mongodb.DBCollection;
import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MWindow;

import net.p2pexchangehub.client.web.components.OfferGrid;
import net.p2pexchangehub.client.web.form.OfferEditor;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.MatchExchangeOfferCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.domain.Offer;

@CDIView(OfferView.VIEW_NAME)
public class OfferView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "OffersView";

    @Inject
    private MongoTemplate mongoTemplate;
    
    @Inject
    private CommandGateway gateway;
    
    @Inject
    private OfferGrid offerGrid;
    
    private String userAccountId = "usac1"; //TODO variable
    
    @PostConstruct
    private void init() {
        setSizeFull();

        GridContextMenu offerContextMenu = offerGrid.getContextMenu();
        offerContextMenu.addGridBodyContextMenuListener(e -> {
            Object itemId = e.getItemId();
            if (itemId!=null) {
                Offer offer = offerGrid.getMongoContainerDataSource().getItem(itemId).getBean();
                if (offer.getState()==OfferState.UNPAIRED) {
                    offerContextMenu.addSeparator();
                    offerContextMenu.addItem("Match with offer", mc -> {
                        BigDecimal amountRequested = offer.getAmountOfferedMin();
                        BigDecimal amountOffered = amountRequested.multiply(offer.getAmountRequestedExchangeRateFormula()).setScale(1, RoundingMode.UP);                        
                        gateway.send(new MatchExchangeOfferCommand(UUID.randomUUID().toString(), offer.getId(), "usac2", amountOffered, amountRequested));
                    });
                }
            }
        });
        
        
        addComponent(offerGrid);
        setExpandRatio(offerGrid, 1.0f);

        MHorizontalLayout c = new MHorizontalLayout();
        addComponent(c);
        c.addComponent(new Button("Refresh", e -> refreshOffers()));
        c.addComponent(new Button("New", e -> createNew()));
    }

    private void createNew() {
        DBCollection collection = mongoTemplate.getCollection(mongoTemplate.getCollectionName(BankAccount.class));
        List<String> availableCurrencies = collection.distinct(BankAccount.PROPERTY_CURRENCY, new Query(new Criteria().where(BankAccount.PROPERTY_ACTIVE).is(Boolean.TRUE)).getQueryObject());

        OfferEditor editor = new OfferEditor(availableCurrencies);
        Window window = new MWindow("New offer", editor).withModal(true).withResizable(false)
                .withWidth("30%"); //TODO otherwise goes screen wide
        
        editor.setSavedHandler(offer -> {
            window.close();
            gateway.send(new CreateOfferCommand(UUID.randomUUID().toString(), userAccountId, offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getAmountRequestedExchangeRateFormula()));
        });
        
        editor.setResetHandler(offer -> {
            window.close();            
        });
        
        editor.setEntity(new Offer());
        
        getUI().addWindow(window);
    }

    private void refreshOffers() {
        offerGrid.refresh();
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshOffers();
    }
    
}
