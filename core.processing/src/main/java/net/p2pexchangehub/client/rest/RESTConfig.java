package net.p2pexchangehub.client.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("api")
public class RESTConfig extends Application {

    public RESTConfig() {
        super();
//        getProperties().put(ResteasyContextParameters.RESTEASY_ROLE_BASED_SECURITY, Boolean.TRUE.toString());
    }
    
}
