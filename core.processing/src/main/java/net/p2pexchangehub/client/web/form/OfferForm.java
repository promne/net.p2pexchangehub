package net.p2pexchangehub.client.web.form;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.ui.Component;

import java.math.BigDecimal;
import java.util.Collection;

import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;

import net.p2pexchangehub.client.web.data.StringToBigDecimalValidatorWrapper;
import net.p2pexchangehub.view.domain.Offer;

public class OfferForm extends AbstractForm<Offer> {

    private TypedSelect<String> currencyOffered;
    
    private MTextField amountOfferedMin;

    private MTextField amountOfferedMax;

    private TypedSelect<String> currencyRequested;

    private MTextField requestedExchangeRateExpression;
    
    public OfferForm(Collection<String> currencyAvailable) {
        super();
        
        String requiredError = "Field has to contain a value";
        
        currencyOffered = new TypedSelect<>("Currency offered", currencyAvailable).withValidator(new NullValidator(requiredError, false));
        currencyOffered.setNullSelectionAllowed(false);
        currencyOffered.withValidator(new AbstractValidator<String>("Currency has to differ from requested") {

            @Override
            protected boolean isValidValue(String value) {
                return currencyRequested.getValue()!=value;
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        
        currencyRequested = new TypedSelect<>("Currency requested", currencyAvailable).withValidator(new NullValidator(requiredError, false));
        currencyRequested.setNullSelectionAllowed(false);
        currencyRequested.withValidator(new AbstractValidator<String>("Currency has to differ from offered") {
            
            @Override
            protected boolean isValidValue(String value) {
                return currencyOffered.getValue()!=value;
            }
            
            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        
        amountOfferedMin = new MTextField("Amount offered min")
                .withRequired(true).withRequiredError(requiredError)
                .withValidator(new BigDecimalRangeValidator("Has to contain positive value between 100 - 100 000", BigDecimal.valueOf(100), BigDecimal.valueOf(100_000)));
        
        amountOfferedMax = new MTextField("Amount offered max")
                .withRequired(true).withRequiredError(requiredError)
                .withValidator(new BigDecimalRangeValidator("Has to contain positive value between {minimal} - 100 000", BigDecimal.valueOf(100), BigDecimal.valueOf(100_000)) {

                    @Override
                    public BigDecimal getMinValue() {
                        return amountOfferedMin.isValid() ? (BigDecimal) amountOfferedMin.getConvertedValue() : super.getMinValue();
                    }

                    @Override
                    public String getErrorMessage() {
                        return super.getErrorMessage().replace("{minimal}", amountOfferedMin.isValid() ? amountOfferedMin.getValue() : "100");
                    }
                    
                });

        //TODO: set up upper limit right
        BigDecimalRangeValidator exchangeRateValidator = new BigDecimalRangeValidator("Has to contain positive value smaller than 100", BigDecimal.ZERO, BigDecimal.valueOf(100));
        exchangeRateValidator.setMinValueIncluded(false);
        
        requestedExchangeRateExpression = new MTextField("Exchange rate")
                .withValidator(new StringToBigDecimalValidatorWrapper(exchangeRateValidator));
    }

    @Override
    protected Component createContent() {
        return new MFormLayout(currencyOffered, amountOfferedMin, amountOfferedMax, currencyRequested, requestedExchangeRateExpression, getToolbar()).withMargin(true);
    }

    
}
