package org.openspaces.pu.container.jee.stats;

import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Response;

import javax.servlet.ServletResponse;

/**
 * @author kimchy
 */
public class JettyHttpResponseStatus implements HttpResponseStatus {

    public int getStatus(ServletResponse response) {
        final Response jettyResponse = (response instanceof Response) ? ((Response) response) : HttpConnection.getCurrentConnection().getResponse();
        if (jettyResponse != null) {
            return jettyResponse.getStatus();
        }
        return -1;
    }
}
