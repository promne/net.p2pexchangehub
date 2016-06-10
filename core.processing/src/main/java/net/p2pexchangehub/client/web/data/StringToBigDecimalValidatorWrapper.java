package net.p2pexchangehub.client.web.data;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.BigDecimalRangeValidator;

import java.math.BigDecimal;

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
                castValue = new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                //ignore
            }
        }
        validator.validate(castValue);
    }
    
}
