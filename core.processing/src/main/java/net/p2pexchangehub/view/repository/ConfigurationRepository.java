package net.p2pexchangehub.view.repository;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;

import net.p2pexchangehub.view.domain.ConfigurationItem;

@Singleton
public class ConfigurationRepository {

    public static final String BEAN_NAME = "ConfigurationRepository";
    
    private static final String EMPTY_VALUE = "#EMPTY_VALUE";

    @Inject
    private MongoTemplate mongoTemplate;
    
    @Inject
    private Logger log;

    public void updateValue(String key, String value) {
        DBObject findOne = getCollectionInternal().findOne(key);
        findOne.put(ConfigurationItem.PROPERTY_VALUE, value);
        getCollectionInternal().save(findOne);
    }
    
    private String findInternal(String key) {
        DBObject findOne = getCollectionInternal().findOne(key);
        return findOne==null ? null : findOne.get(ConfigurationItem.PROPERTY_VALUE).toString();
    }

    private DBCollection getCollectionInternal() {
        return mongoTemplate.getCollection(mongoTemplate.getCollectionName(ConfigurationItem.class));
    }
    
    public void save(ConfigurationItem entity) {
        mongoTemplate.save(entity);
    }
    
    public String getValueString(String key) {
        String item = findInternal(key);
        if (item == null) {
            log.warn("Property {} is not configured", key);
            return null;
        }
        return item;
    }

    public String getValueString(String key, String defaultValue) {
        String item = findInternal(key);
        return item==null ? defaultValue : item;
    }
    
    public int getValueInt(String key) {
        return Integer.valueOf(getValueString(key));
    }

    public int getValueInt(String key, int defaultValue) {
        String valueString = getValueString(key, EMPTY_VALUE);
        return valueString==EMPTY_VALUE ? defaultValue : Integer.valueOf(valueString);
    }

    public void deleteAll() {
        getCollectionInternal().drop();
    }
    
}
