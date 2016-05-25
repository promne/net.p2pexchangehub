package net.p2pexchangehub.client.web.form;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

import java.util.Collection;

import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;

import net.p2pexchangehub.view.domain.Offer;

public class OfferEditor extends AbstractForm<Offer> {

    private TypedSelect<String> currencyOffered;
    
    private TextField amountOfferedMin;

    private TextField amountOfferedMax;

    private TypedSelect<String> currencyRequested;

    private TextField amountRequestedExchangeRate;    
    
    public OfferEditor(Collection<String> currencyAvailable) {
        super();
        currencyOffered = new TypedSelect<>("Currency offered", currencyAvailable);
        amountOfferedMin = new TextField("Amount offered min");
        amountOfferedMax = new TextField("Amount offered max");
        currencyRequested = new TypedSelect<>("Currency requested", currencyAvailable);
        amountRequestedExchangeRate = new TextField("Exchange rate");
    }

    @Override
    protected Component createContent() {
        return new MFormLayout(currencyOffered, amountOfferedMin, amountOfferedMax, currencyRequested, amountRequestedExchangeRate, getToolbar());
    }

    
}
