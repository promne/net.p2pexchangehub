package net.p2pexchangehub.core.handler.user;

import java.util.Date;

public class ContactDetail {

    public enum Type {
        EMAIL,
        PHONE
    }

    private final String id;
    
    private final Type type;
    
    private final String value;

    private boolean confirmed;
    
    private String validationCode;
    
    private Date validationCodeExpiration;

    public ContactDetail(String id, Type type, String value) {
        super();
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String confirmationCode) {
        this.validationCode = confirmationCode;
    }

    public Date getValidationCodeExpiration() {
        return validationCodeExpiration;
    }

    public void setValidationCodeExpiration(Date confirmationCodeExpiration) {
        this.validationCodeExpiration = confirmationCodeExpiration;
    }

}
