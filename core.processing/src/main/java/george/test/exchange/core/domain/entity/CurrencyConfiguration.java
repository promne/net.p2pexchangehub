package george.test.exchange.core.domain.entity;

import org.springframework.data.annotation.Id;

public class CurrencyConfiguration {

    @Id
    private String code;
    
    private int scale;

    public CurrencyConfiguration() {
        super();
    }

    public CurrencyConfiguration(String code, int precision) {
        super();
        this.code = code;
        this.scale = precision;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int precision) {
        this.scale = precision;
    }
        
}
