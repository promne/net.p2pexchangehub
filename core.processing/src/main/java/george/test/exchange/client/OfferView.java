package george.test.exchange.client;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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
import esw.view.UserAccountView;
import george.test.exchange.client.components.OfferGrid;
import george.test.exchange.client.form.OfferEditor;
import george.test.exchange.core.domain.offer.OfferState;

@CDIView(OfferView.VIEW_NAME)
public class OfferView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "OffersView";

    @PersistenceContext
    private EntityManager em;

    @Inject
    private BankAccountView bankAccountView;
    
    @Inject
    private UserAccountView userAccountView;
    
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
                Offer offer = offerGrid.getJPAContainerDataSource().getItem(itemId).getEntity();
                if (offer.getState()==OfferState.UNPAIRED) {
                    offerContextMenu.addSeparator();
                    offerContextMenu.addItem("Match with offer", mc -> {
                        BigDecimal amountRequested = offer.getAmountOfferedMin();
                        BigDecimal amountOffered = amountRequested.multiply(offer.getAmountRequestedExchangeRate()).setScale(1, RoundingMode.UP);                        
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
        OfferEditor editor = new OfferEditor(bankAccountView.listAvailableCurrencies());
        Window window = new MWindow("New offer", editor).withModal(true).withResizable(false)
                .withWidth("30%"); //TODO otherwise goes screen wide
        
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
        offerGrid.refresh();
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshOffers();
    }
    
}
