package net.p2pexchangehub.view.domain;

public class UserAccountContact {

    public enum Type {
        EMAIL,
        PHONE
    }
    
    private String value;
    public static final String PROPERTY_VALUE = "value";
    
    private Type type;
    
    private boolean validated;

    public UserAccountContact(String value, Type type) {
        super();
        this.value = value;
        this.type = type;
    }

    public UserAccountContact() {
        super();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
}
