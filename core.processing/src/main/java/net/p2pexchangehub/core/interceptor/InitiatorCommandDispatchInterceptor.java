package net.p2pexchangehub.core.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.axonframework.commandhandling.CommandDispatchInterceptor;
import org.axonframework.commandhandling.CommandMessage;

import net.p2pexchangehub.client.web.security.UserIdentity;

public class InitiatorCommandDispatchInterceptor implements CommandDispatchInterceptor {

    public static final String METADATA_KEY = "userAccountId";
    
    @Inject
    private Instance<UserIdentity> userIdentity;
    
    @Override
    public CommandMessage<?> handle(CommandMessage<?> commandMessage) {
        CommandMessage<?> result = commandMessage;
        try {
            Map<String, Object> metaData = new HashMap<>();
            if (!userIdentity.isUnsatisfied()) {
                metaData.put(METADATA_KEY, userIdentity.get().getUserAccountId());            
            }        
            return commandMessage.andMetaData(metaData);
        } catch (ContextNotActiveException e) {
            // do nothing
        }
        return result;
    }
    
}
