package org.openspaces.admin;

import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;

/**
 * @author kimchy
 */
public interface Transport {

    String getUID();

    String getHost();
    
    int getPort();

    TransportConfiguration getConfiguration();

    TransportStatistics getStatistics();
}
