package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OSDetails;
import org.openspaces.admin.os.OperatingSystemDetails;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemDetails implements OperatingSystemDetails {

    private final OSDetails details;

    public DefaultOperatingSystemDetails(OSDetails details) {
        this.details = details;
    }

    public boolean isNA() {
        return details.isNA();
    }

    public String getUID() {
        return details.getUID();
    }

    public String getName() {
        return details.getName();
    }

    public String getArch() {
        return details.getArch();
    }

    public String getVersion() {
        return details.getVersion();
    }

    public int getAvailableProcessors() {
        return details.getAvailableProcessors();
    }
}
