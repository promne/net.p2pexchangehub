package net.p2pexchangehub.client.web.data;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.ui.UI;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

public class StringToBigDecimalValidatorWrapper implements Validator {

    private final BigDecimalRangeValidator validator;

    public StringToBigDecimalValidatorWrapper(BigDecimalRangeValidator validator) {
        super();
        this.validator = validator;
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        Object castValue = value;
        if (value!=null) {
            try {
                Number number = NumberFormat.getInstance(UI.getCurrent().getLocale()).parse(value.toString());
                castValue = BigDecimal.valueOf(number.doubleValue());
            } catch (NumberFormatException | ParseException e) {
                //ignore
            }
        }
        validator.validate(castValue);
    }
    
}
