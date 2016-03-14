package george.test.exchange.client;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

import george.test.exchange.core.domain.entity.ExchangeOffer;
import george.test.exchange.core.processing.service.ExchangeOfferService;

@CDIView(OffersView.VIEW_NAME)
public class OffersView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "OffersView";

    @Inject
    private ExchangeOfferService offerService;
    
    private BeanItemContainer<ExchangeOffer> offersContainer = new BeanItemContainer<>(ExchangeOffer.class);

    public OffersView() {
        super();
        setSizeFull();
        
        Grid offerGrid= new Grid("Offers", offersContainer);
        offerGrid.setSizeFull();
        addComponent(offerGrid);
        setExpandRatio(offerGrid, 1.0f);

        addComponent(new Button("Refresh", e -> refreshOffers()));
    }

    private void refreshOffers() {
        offersContainer.removeAllItems();
        offersContainer.addAll(offerService.listAllOffers());        
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshOffers();
    }
    
}
