package george.test.exchange.rest.client;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {

    public JacksonJsonProvider() {
        super();
        ObjectMapper mapper = _mapperConfig.getDefaultMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        super.setMapper(mapper);
    }

}

