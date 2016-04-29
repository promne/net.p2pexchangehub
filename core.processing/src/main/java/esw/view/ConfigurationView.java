package esw.view;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import esw.domain.ConfigurationItem;

@Stateless
public class ConfigurationView {

    public static final String BEAN_NAME = "ConfigurationView";
    
    private static final String EMPTY_VALUE = "#EMPTY_VALUE";
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private Logger log;

    public String getValueString(String key) {
        ConfigurationItem item = em.find(ConfigurationItem.class, key);
        if (item == null) {
            log.warn("Property {} is not configured", key);
            return null;
        }
        return item.getValue();
    }

    public String getValueString(String key, String defaultValue) {
        ConfigurationItem item = em.find(ConfigurationItem.class, key);
        return item==null ? defaultValue : item.getValue();
    }
    
    public int getValueInt(String key) {
        return Integer.valueOf(getValueString(key));
    }

    public int getValueInt(String key, int defaultValue) {
        String valueString = getValueString(key, EMPTY_VALUE);
        return valueString==EMPTY_VALUE ? defaultValue : Integer.valueOf(valueString);
    }
    
}
