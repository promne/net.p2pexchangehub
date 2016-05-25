package net.p2pexchangehub.client.web.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.ViewScoped;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import net.p2pexchangehub.core.api.offer.CancelExchangeOfferCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@ViewScoped
public class OfferGrid extends MongoGrid<Offer> {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;

    public OfferGrid() {
        super(Offer.class);

        setCellStyleGenerator(cellRef -> {
            Offer offer = getEntity(cellRef.getItemId());
            if (Arrays.asList(OfferState.EXCHANGE_COMPLETE, OfferState.DEBIT_REQUESTED, OfferState.WAITING_FOR_PAYMENT).contains(offer.getState())) {
                return THEME_STYLE_GOOD;
            }
            if (OfferState.UNPAIRED == offer.getState()) {
                return THEME_STYLE_GOOD_HIGHLIGHT;
            }
            if (Arrays.asList(OfferState.CLOSED, OfferState.CANCELED).contains(offer.getState())) {
                return null;
            }
            return THEME_STYLE_ERROR;
        } );        
        
        GridContextMenu offerContextMenu = getContextMenu();
        offerContextMenu.addGridBodyContextMenuListener(e -> {
            offerContextMenu.removeItems();
            offerContextMenu.addItem("Refresh", c -> refresh());

            Offer offer = getEntity(e.getItemId());
            if (offer!=null) {
                if (offer.getState()==OfferState.UNPAIRED) {
                    offerContextMenu.addItem("Cancel", c -> gateway.send(new CancelExchangeOfferCommand(offer.getId())));                                        
                }
            }
        });        
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        this.setCaption("Offers");
    }

    @Override
    protected Class<Offer> getItemClass() {
        return Offer.class;
    }

    
}
