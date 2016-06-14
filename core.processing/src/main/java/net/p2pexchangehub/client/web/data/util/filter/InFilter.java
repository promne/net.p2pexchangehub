package net.p2pexchangehub.client.web.data.util.filter;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import java.util.ArrayList;
import java.util.Collection;

public class InFilter implements Filter {

    private final Object propertyId;
    private final Collection<Object> values;

    public InFilter(Object propertyId, Collection<?> values) {
        super();
        this.propertyId = propertyId;
        this.values = new ArrayList<>(values);
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
        final Property<?> p = item.getItemProperty(propertyId);
        if (p == null) {
            return false;
        }
        Object propertyValue = p.getValue();
        if (propertyValue == null) {
            return false;
        }
        return values.contains(propertyValue);
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
        return this.propertyId.equals(propertyId);
    }

    public Object getPropertyId() {
        return propertyId;
    }

    public Collection<Object> getValues() {
        return values;
    }
    
}
