package net.p2pexchangehub.client.rest.security;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.util.Base64;

import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.processing.service.AuthenticationService;
import net.p2pexchangehub.client.security.AllowAll;
import net.p2pexchangehub.client.security.AllowRoles;
import net.p2pexchangehub.view.domain.UserAccount;

/**
 * This interceptor verify the access permissions for a user based on username
 * and passowrd provided in request
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityInterceptor implements javax.ws.rs.container.ContainerRequestFilter {
    
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());

    @Inject 
    private UserIdentityRest userIdentity;

    @Inject
    private AuthenticationService authenticationService;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();
        
        Optional<UserAccount> authenticatedUser = extractUserIdentity(requestContext);
        if (authenticatedUser.isPresent()) {
            userIdentity.setUserAccount(authenticatedUser.get());
        }

        // Access allowed for all       
        if (method.isAnnotationPresent(AllowAll.class)) {
            return;
        }
        
        //everything requires valid and enabled user
        if (!authenticatedUser.isPresent() || !authenticatedUser.get().isEnabled()) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }
        
        // Verify user access
        if (method.isAnnotationPresent(AllowRoles.class)) {
            AllowRoles rolesAnnotation = method.getAnnotation(AllowRoles.class);
            Set<UserAccountRole> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
            
            // Is user valid?
            if (!CollectionUtils.containsAny(authenticatedUser.get().getRoles(), rolesSet)) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }
        }
    }

    private Optional<UserAccount> extractUserIdentity(ContainerRequestContext requestContext) {
        // Get request headers
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        
        // Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
        
        // If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            return Optional.empty();
        }
        
        // Get encoded username and password
        final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
        
        // Decode username and password
        String[] usernameAndPassword = null;
        try {
            usernameAndPassword = new String(Base64.decode(encodedUserPassword)).split(":");
        } catch (IOException e) {
            return Optional.empty();
        }

        if (usernameAndPassword.length!=2) {
            return Optional.empty();
        }
        return authenticationService.authenticate(usernameAndPassword[0], usernameAndPassword[1]);
    }
    

}
