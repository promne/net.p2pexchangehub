package net.p2pexchangehub.client.web.data;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.function.Supplier;

public class StringToBigDecimalFrictionLimitConverter extends com.vaadin.data.util.converter.StringToBigDecimalConverter {

    private static final long serialVersionUID = 1L;
    
    private final Supplier<Integer> maximumFrictionDigitsSupplier;
    
    public StringToBigDecimalFrictionLimitConverter() {
        this(() -> 340);
    }

    public StringToBigDecimalFrictionLimitConverter(String currencyCode) {
        this(() -> Currency.getInstance(currencyCode).getDefaultFractionDigits());
    }
    
    public StringToBigDecimalFrictionLimitConverter(Supplier<Integer> maximumFrictionDigitsSupplier) {
        super();
        this.maximumFrictionDigitsSupplier = maximumFrictionDigitsSupplier;
    }

    @Override
    protected NumberFormat getFormat(Locale locale) {
        NumberFormat format = super.getFormat(locale);
        format.setMaximumFractionDigits(maximumFrictionDigitsSupplier.get());
        return format;
    }

}
