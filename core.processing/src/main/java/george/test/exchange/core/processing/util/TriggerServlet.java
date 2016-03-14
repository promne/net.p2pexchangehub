package george.test.exchange.core.processing.util;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.Path;

import george.test.exchange.core.processing.scheduler.ExternalBankAccountImportScheduler;

@Path("/trigger")
public class TriggerServlet extends HttpServlet {

    @Inject
    private ExternalBankAccountImportScheduler scheduler;
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        scheduler.listAccountStatements();
        super.service(req, res);
    }

}
