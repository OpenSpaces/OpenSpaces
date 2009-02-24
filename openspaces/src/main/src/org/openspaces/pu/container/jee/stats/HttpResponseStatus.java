package org.openspaces.pu.container.jee.stats;

import javax.servlet.ServletResponse;

/**
 * @author kimchy
 */
public interface HttpResponseStatus {

    int getStatus(ServletResponse response);
}
