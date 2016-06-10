package net.p2pexchangehub.client.web.helpdesk;

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
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MWindow;

import net.p2pexchangehub.client.web.components.OfferGrid;
import net.p2pexchangehub.client.web.form.OfferForm;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.MatchExchangeOfferCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.core.util.ExchangeRateEvaluator;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.repository.BankAccountRepositoryHelper;

@CDIView(OfferView.VIEW_NAME)
@RolesAllowed("admin")
public class OfferView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "OffersView";

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private BankAccountRepositoryHelper bankAccountRepositoryHelper;
    
    @Inject
    private OfferGrid offerGrid;
    
    @Inject
    private ExchangeRateEvaluator exchangeRateEvaluator;
    
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
                        BigDecimal requestedExchangeRate = exchangeRateEvaluator.evaluate(offer.getRequestedExchangeRateExpression());
                        BigDecimal amountOffered = amountRequested.multiply(requestedExchangeRate).setScale(1, RoundingMode.UP);                        
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
        List<String> availableCurrencies = bankAccountRepositoryHelper.listAvailableCurrencies();

        OfferForm editor = new OfferForm(availableCurrencies);
        Window window = new MWindow("New offer", editor).withModal(true).withResizable(false)
                .withWidth("30%"); //TODO otherwise goes screen wide
        
        editor.setSavedHandler(offer -> {
            window.close();
            gateway.send(new CreateOfferCommand(UUID.randomUUID().toString(), userAccountId, offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression()));
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
