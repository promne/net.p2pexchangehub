package net.p2pexchangehub.client.web.helpdesk;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import net.p2pexchangehub.client.web.components.OfferGrid;

@CDIView(OfferView.VIEW_NAME)
@RolesAllowed("admin")
public class OfferView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "OffersView";

    @Inject
    private OfferGrid offerGrid;
    
    @PostConstruct
    private void init() {
        setSizeFull();
//        offerGrid.setSizeFull();
        addComponent(offerGrid);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        offerGrid.refresh();
    }
    
}
