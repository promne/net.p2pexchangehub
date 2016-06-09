package net.p2pexchangehub.core.handler.external.bank;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExternalBankAccountNumberValidator {

    private final static Map<String, String> currencyBankAccountPatterns;
    
    static {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("CZK", "^(?:(\\d{1,6})-)?(\\d{2,10})/(\\d{4})$");
        patterns.put("NZD", "^(\\d{2})-(\\d{4})-(\\d{7})-(\\d{2,3})$");
        
        currencyBankAccountPatterns = Collections.unmodifiableMap(patterns);
    }
    
    public String getValidationPattern(String currency) {
        return currencyBankAccountPatterns.get(currency);
    }
    
}
