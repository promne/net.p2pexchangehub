package net.p2pexchangehub.client.web.components;

import com.vaadin.data.validator.AbstractStringValidator;

public class StringEqualsValidator extends AbstractStringValidator {

    private static final long serialVersionUID = 1L;
    
    private final String expectedValue;
    
    public StringEqualsValidator(String expectedValue, String errorMessage) {
        super(errorMessage);
        this.expectedValue = expectedValue;
    }


    @Override
    protected boolean isValidValue(String value) {
        return expectedValue.equals(value);
    }

}
