package org.openspaces.admin;

import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import com.gigaspaces.lrmi.nio.info.TransportStatistics;

/**
 * @author kimchy
 */
public interface TransportInfoProvider {

    TransportConfiguration getTransportConfiguration() throws AdminException;

    TransportStatistics getTransportStatistics() throws AdminException;
}
