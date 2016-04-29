package george.test.exchange.core.processing.util;

import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventCountSnapshotterTrigger;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.SnapshotterTrigger;
import org.axonframework.eventstore.SnapshotEventStore;
import org.axonframework.eventstore.jpa.JpaEventStore;
import org.axonframework.integration.cdi.AutoConfigure;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.repository.inmemory.InMemorySagaRepository;

@ApplicationScoped
public class AxonConfiguration {

    @PersistenceContext
    private EntityManager em;

    @Produces
    @AutoConfigure
    @ApplicationScoped
    public EventBus eventBus() {
        return new SimpleEventBus();
    }

    @Produces
    @ApplicationScoped
    public SnapshotEventStore eventStore() {
        return new JpaEventStore(new SimpleEntityManagerProvider(em));
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
    public AggregateSnapshotter snapshotter(SnapshotEventStore snapshotEventStore) {
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

}
