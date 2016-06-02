package net.p2pexchangehub.core.util;

import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandDispatchInterceptor;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.DefaultClusterSelector;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleCluster;
import org.axonframework.eventhandling.replay.BackloggingIncomingMessageHandler;
import org.axonframework.eventhandling.replay.ReplayingCluster;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventCountSnapshotterTrigger;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.SnapshotterTrigger;
import org.axonframework.eventstore.management.EventStoreManagement;
import org.axonframework.eventstore.mongo.DefaultMongoTemplate;
import org.axonframework.eventstore.mongo.MongoEventStore;
import org.axonframework.eventstore.mongo.MongoTemplate;
import org.axonframework.integration.cdi.AutoConfigure;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.repository.inmemory.InMemorySagaRepository;
import org.axonframework.unitofwork.NoTransactionManager;

@ApplicationScoped
public class AxonConfiguration {

    @Produces
    @AutoConfigure
    @ApplicationScoped
    public EventBus eventBus(Cluster cluster) {
        return new ClusteringEventBus(new DefaultClusterSelector(cluster));
    }

    
    @Produces
    @Singleton
    public ReplayingCluster defaultCluster(EventStoreManagement eventStoreManagement) {
        return new ReplayingCluster(
                new SimpleCluster("default"),
                eventStoreManagement,
                new NoTransactionManager(),
                0,
                new BackloggingIncomingMessageHandler());
    }

    @Produces
    @Singleton
    public MongoEventStore defaultMongoEventStore() throws UnknownHostException {
        MongoClient mongo = new MongoClient("192.168.56.101");
        char[] password = null;
        String userName = null;
        String snapshotEventsCollectionName = "snapshotevents";
        String domainEventsCollectionName = "domainevents";
        String databaseName = "exchange";
        MongoTemplate mongoTemplate = new DefaultMongoTemplate(mongo, databaseName, domainEventsCollectionName, snapshotEventsCollectionName, userName, password);
        return new MongoEventStore(mongoTemplate);        
    }
    
    @Produces
    @ApplicationScoped
    public EventScheduler eventScheduler(EventBus eventBus) {
        return new SimpleEventScheduler(Executors.newSingleThreadScheduledExecutor(), eventBus);
    }

    @Produces
    @AutoConfigure
    @ApplicationScoped
    public SagaRepository sagaRepository() {
        return new InMemorySagaRepository();
    }

    @Produces
    @AutoConfigure
    @ApplicationScoped
    public CommandBus commandBus() {
        SimpleCommandBus simpleCommandBus = new SimpleCommandBus();
//        simpleCommandBus.setRollbackConfiguration(new RollbackOnAllExceptionsConfiguration());
        return simpleCommandBus;
    }

    // Snapshots
    @Produces
    @AutoConfigure
    @ApplicationScoped
    public AggregateSnapshotter snapshotter() {
        return new AggregateSnapshotter();
    }

    @Produces
    @ApplicationScoped
    public SnapshotterTrigger snapshotterTrigger(Snapshotter snapshotter) {
        EventCountSnapshotterTrigger trigger = new EventCountSnapshotterTrigger();
        trigger.setTrigger(10);
        trigger.setSnapshotter(snapshotter);
        return trigger;
    }

    @Produces
    public CommandGateway commandGateway(CommandBus commandBus, Instance<CommandDispatchInterceptor> commandDispatchInterceptors) {
        CommandDispatchInterceptor[] interceptorsArray = StreamSupport.stream(commandDispatchInterceptors.spliterator(), false).collect(Collectors.toList()).toArray(new CommandDispatchInterceptor[]{});
        return new DefaultCommandGateway(commandBus, interceptorsArray);
    }
}
