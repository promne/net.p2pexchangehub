package net.p2pexchangehub.client.web.data.util.filter;

import com.vaadin.data.Container.Filter;

import org.springframework.data.mongodb.core.query.Criteria;
import org.tylproject.vaadin.addon.utils.DefaultFilterConverter;

public class MongoFilterConverter extends DefaultFilterConverter {

    @Override
    public Criteria convert(Filter f, boolean negated) {
        if (f instanceof InFilter) {
            return convertInFilter((InFilter) f, negated);
        }
        return super.convert(f, negated);
    }

    private Criteria convertInFilter(InFilter f, boolean negated) {
        Criteria c = Criteria.where(f.getPropertyId().toString());
        if (negated) {
            c = c.not();
        }
        c.in(f.getValues());
        return c;
    }

}
