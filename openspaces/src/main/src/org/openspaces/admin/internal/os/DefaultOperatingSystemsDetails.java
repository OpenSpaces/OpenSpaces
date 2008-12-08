package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemsDetails;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsDetails implements OperatingSystemsDetails {

    private final OperatingSystemDetails[] details;

    public DefaultOperatingSystemsDetails(OperatingSystemDetails[] details) {
        this.details = details;
    }

    public int getAvailableProcessors() {
        int total = 0;
        for (OperatingSystemDetails detail : details) {
            total += detail.getAvailableProcessors();
        }
        return total;
    }
}
