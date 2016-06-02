package net.p2pexchangehub.client.web.components;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertyValueGenerator;

public class ConstantPropertyValueGenerator<T> extends PropertyValueGenerator<T> {

    private static final long serialVersionUID = -2189996363132177609L;
    
    private final T value;

    private final Class<T> classType;
    
    @SuppressWarnings("unchecked")
    public ConstantPropertyValueGenerator(T value) {
        this((Class<T>) value.getClass(), value);
    }

    public ConstantPropertyValueGenerator(Class<T> classType, T value) {
        super();
        this.value = value;
        this.classType = classType;
    }

    @Override
    public T getValue(Item item, Object itemId, Object propertyId) {
        return value;
    }

    @Override
    public Class<T> getType() {
        return classType;
    }

}
