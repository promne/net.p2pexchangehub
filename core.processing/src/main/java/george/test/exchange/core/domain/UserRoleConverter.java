package george.test.exchange.core.domain;

import java.util.Arrays;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

@Converter//(autoApply=true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    private static final BidiMap<UserRole, String> CONVERTER_MAP;
    
    static {
        CONVERTER_MAP = new DualHashBidiMap<>();
        CONVERTER_MAP.put(UserRole.ADMIN, "ADMIN");
        CONVERTER_MAP.put(UserRole.OPERATOR, "OPERATOR");
        
        if (!CONVERTER_MAP.keySet().containsAll(Arrays.asList(UserRole.values())) || CONVERTER_MAP.values().size()!=UserRole.values().length) {
            throw new IllegalStateException(CONVERTER_MAP.toString());
        }
    }
    
    public UserRoleConverter() {
        super();
    }

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return CONVERTER_MAP.get(attribute);
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return CONVERTER_MAP.getKey(dbData);
    }

}
