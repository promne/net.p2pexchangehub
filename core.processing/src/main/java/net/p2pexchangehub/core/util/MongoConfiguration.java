package net.p2pexchangehub.core.util;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongoConfiguration {

    @Inject
    private Logger logger;
    
    @Produces
    @Singleton
    public MongoClient createMongoClient() throws UnknownHostException {
        String mongoIps = System.getProperty("p2pexchangehub.mongo.ip");
        logger.info("Creating connection to mongodb: {}", mongoIps);
        List<ServerAddress> serverAddress = new ArrayList<>();
        for (String addr : mongoIps.split(",")) {
            serverAddress.add(new ServerAddress(addr));
        }
        return new MongoClient(serverAddress);
    }
    
    @Produces
    @Singleton
    public MongoTemplate createMongoTemplate(MongoClient mongoClient) {
        String databaseName = "exchangeews";
        return new MongoTemplate(mongoClient, databaseName);
    }

}
