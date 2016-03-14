package george.test.exchange.core.domain;

import java.util.Arrays;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

@Converter(autoApply=true)
public class ExternalBankTypeConverter implements AttributeConverter<ExternalBankType, String> {

    private static final BidiMap<ExternalBankType, String> CONVERTER_MAP;
    
    static {
        CONVERTER_MAP = new DualHashBidiMap<>();
        CONVERTER_MAP.put(ExternalBankType.TEST, "TEST");
        CONVERTER_MAP.put(ExternalBankType.FIO, "FIO");
        
        if (!CONVERTER_MAP.keySet().containsAll(Arrays.asList(ExternalBankType.values())) || CONVERTER_MAP.values().size()!=ExternalBankType.values().length) {
            throw new IllegalStateException(CONVERTER_MAP.toString());
        }
    }
    
    
    @Override
    public String convertToDatabaseColumn(ExternalBankType attribute) {
        return CONVERTER_MAP.get(attribute);
    }

    @Override
    public ExternalBankType convertToEntityAttribute(String dbData) {
        return CONVERTER_MAP.getKey(dbData);
    }

}
