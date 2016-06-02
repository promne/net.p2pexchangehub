package net.p2pexchangehub.core.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.axonframework.commandhandling.CommandDispatchInterceptor;
import org.axonframework.commandhandling.CommandMessage;

public class ServletCommandDispatchInterceptor implements CommandDispatchInterceptor {

    public static final String METADATA_KEY_REMOTE_ADDR = "remoteAddr";
    
    @Inject
    private Instance<HttpServletRequest> servletRequest;
    
    @Override
    public CommandMessage<?> handle(CommandMessage<?> commandMessage) {
        CommandMessage<?> result = commandMessage;
        try {
            if (!servletRequest.isUnsatisfied()) { 
                Map<String, Object> metaData = new HashMap<>();                
                metaData.put(METADATA_KEY_REMOTE_ADDR, servletRequest.get().getRemoteAddr());
                result = commandMessage.andMetaData(metaData);
            }            
        } catch (IllegalStateException e) {
            //nothing, weld acting crazy? It says isUnsatisfied==false and then fails
        }
        return result;
    }

}
