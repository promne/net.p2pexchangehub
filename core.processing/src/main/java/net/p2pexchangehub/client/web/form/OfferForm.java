package net.p2pexchangehub.client.web.form;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Currency;

import org.vaadin.viritin.MBeanFieldGroup;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MFormLayout;

import net.p2pexchangehub.client.web.data.StringToBigDecimalFrictionLimitConverter;
import net.p2pexchangehub.core.util.ExchangeRateEvaluator;
import net.p2pexchangehub.view.domain.Offer;

public class OfferForm extends AbstractForm<Offer> {

    private TypedSelect<String> currencyOffered;
    
    private MTextField amountOfferedMin;

    private MTextField amountOfferedMax;

    private TypedSelect<String> currencyRequested;

    private MTextField exchangeRate;
    
    private MLabel amountRequestedReadable;
    
    private ExchangeRateEvaluator exchangeRateEvaluator;
    
    public OfferForm(Collection<String> currencyAvailable, ExchangeRateEvaluator currencyService) {
        super();
        
        setResetHandler(offer -> {
            OfferForm.this.closePopup();            
        });
        
        this.exchangeRateEvaluator = currencyService;
        
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

        exchangeRate = new MTextField("Exchange rate")
                .withRequired(true).withRequiredError(requiredError);
        
        amountRequestedReadable = new MLabel("You will get", null);
    }

    @Override
    public MBeanFieldGroup<Offer> setEntity(Offer entity) {
        MBeanFieldGroup<Offer> result = super.setEntity(entity);
        result.withEagerValidation(e -> {
            this.onFieldGroupChange(e);
            boolean valid = e.isValid();
            if (valid) {
                BigDecimal requestedExchangeRateValue = (BigDecimal) exchangeRate.getConvertedValue();
                String currencyRequestedString = currencyRequested.getValue();
                String currencyOfferedString = currencyOffered.getValue();
                
                NumberFormat numberFormat = NumberFormat.getInstance(UI.getCurrent().getLocale());
                String amountMinReadable = numberFormat.format(exchangeRateEvaluator
                        .calculateExchangePay((BigDecimal) amountOfferedMin.getConvertedValue(), currencyOfferedString, currencyRequestedString, requestedExchangeRateValue.toPlainString())
                        );
                String amountMaxReadable = exchangeRateEvaluator
                        .calculateExchangePay((BigDecimal) amountOfferedMax.getConvertedValue(), currencyOfferedString, currencyRequestedString, requestedExchangeRateValue.toPlainString())
                        .toPlainString();
                
                String requestedAmount = (amountMinReadable.equals(amountMaxReadable) ? amountMinReadable : (amountMinReadable + " - " + amountMaxReadable)) + " " + currencyRequestedString;
                amountRequestedReadable.setValue(requestedAmount);
            } else {
                amountRequestedReadable.setValue("???");
            }
        });
        StringToBigDecimalFrictionLimitConverter currencyConverter = new StringToBigDecimalFrictionLimitConverter(() -> Currency.getInstance(currencyOffered.getValue()).getDefaultFractionDigits());
        amountOfferedMin.setConverter(currencyConverter);
        amountOfferedMax.setConverter(currencyConverter);
        exchangeRate.setConverter(new StringToBigDecimalFrictionLimitConverter(() -> ExchangeRateEvaluator.RATE_PRECISION));
        
        return result;
    }

    @Override
    protected Component createContent() {
        MFormLayout mFormLayout = new MFormLayout(currencyOffered, amountOfferedMin, amountOfferedMax, currencyRequested, exchangeRate, amountRequestedReadable, getToolbar())
                .withMargin(true);
        mFormLayout.setSizeUndefined();
        return mFormLayout;
    }

    
}
