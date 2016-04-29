package george.test.exchange.core.processing.util;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;

@Singleton
public class CommandGatewayTransactional implements CommandGateway {

    @Inject
    private CommandBus commandBus;
        
    private CommandGateway gateway;
    
    @PostConstruct
    private void init() {
        gateway = new DefaultCommandGateway(commandBus);
    }
    
    @Override
    public <R> void send(Object command, CommandCallback<R> callback) {
        gateway.send(command, callback);
    }

    @Override
    public <R> R sendAndWait(Object command) {
        return gateway.sendAndWait(command);
    }

    @Override
    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        return gateway.sendAndWait(command, timeout, unit);
    }

    @Override
    public void send(Object command) {
        gateway.send(command);
    }
}
