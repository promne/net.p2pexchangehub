package net.p2pexchangehub.core.util;

import com.mongodb.MongoClient;

import java.net.UnknownHostException;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.springframework.data.mongodb.core.MongoTemplate;

public class MongoConfiguration {

    @Produces
    @Singleton
    public MongoTemplate createMongoTemplate() throws UnknownHostException {
        String databaseName = "exchangeews";
        MongoClient mongoClient = new MongoClient("192.168.56.101");
        return new MongoTemplate(mongoClient, databaseName);
    }

}
