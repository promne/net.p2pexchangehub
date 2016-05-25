package george.test.exchange.core.processing.service.bank.provider.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.axonframework.commandhandling.gateway.CommandGateway;

import net.p2pexchangehub.core.api.external.bank.LogExternalBankAccountCommunicationCommand;

public class MyClientRequestFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    public static final Charset UTF8 = Charset.forName("UTF-8");
    
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";
    private static final String NOTIFICATION_PREFIX = "* ";
    
    private static final String ENTITY_LOGGER_PROPERTY = MyClientRequestFilter.class.getName() + ".entityLogger";
    
    private final int maxEntitySize = 8 * 1024;

    @Inject
    private MyClientRequestFilterBean bean;
    
    @Inject
    private CommandGateway gateway;
    
    public MyClientRequestFilter() {
        super();
    }

    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        final StringBuilder b = new StringBuilder();

        b.append(context.getMethod()).append(" ").append(context.getUri().toASCIIString()).append("\n");
        printHeaders(b, context.getStringHeaders());

        if (context.hasEntity()) {
            final OutputStream stream = new LoggingStream(b, context.getEntityStream());
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }
    }    
    

    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            log(stream.getStringBuilder(getCharset(writerInterceptorContext.getMediaType())));
        }
    }    
    
    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) throws IOException {
        final StringBuilder b = new StringBuilder();

        b.append(Integer.toString(responseContext.getStatus())).append("\n");
        printHeaders(b, responseContext.getHeaders());

        if (responseContext.hasEntity()) {
            responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream(), getCharset(responseContext.getMediaType())));
        }

        log(b);
    }

    private InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = stream.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize), charset));
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }
    
    private static Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? UTF8 : Charset.forName(name);
    }
    
    private void printHeaders(final StringBuilder b, final MultivaluedMap<String, String> headers) {
        for (final Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            final List<?> val = headerEntry.getValue();
            final String header = headerEntry.getKey();

            if (val.size() == 1) {
                b.append(header).append(": ").append(val.get(0)).append("\n");
            } else {
                final StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (final Object s : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(s);
                }
                b.append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }

    private void log(final StringBuilder b) {
        gateway.send(new LogExternalBankAccountCommunicationCommand(bean.getBankAccountId(), b.toString()));
    }

    private class LoggingStream extends FilterOutputStream {

        private final StringBuilder b;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LoggingStream(final StringBuilder b, final OutputStream inner) {
            super(inner);

            this.b = b;
        }

        StringBuilder getStringBuilder(final Charset charset) {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, maxEntitySize), charset));
            if (entity.length > maxEntitySize) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= maxEntitySize) {
                baos.write(i);
            }
            out.write(i);
        }
    }    
}
