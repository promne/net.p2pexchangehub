package esw.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ConfigurationItem {

    @Id
    private String name;
    public static final String PROPERTY_NAME = "name";
    
    private String value;
    public static final String PROPERTY_VALUE = "value";

    public ConfigurationItem() {
        super();
    }

    public ConfigurationItem(String name) {
        super();
        this.name = name;
    }

    public ConfigurationItem(String key, String value) {
        super();
        this.name = key;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String key) {
        this.name = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
