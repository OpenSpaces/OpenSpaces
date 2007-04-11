package org.openspaces.pu.container.servicegrid.deploy;

import org.springframework.core.NestedRuntimeException;

import java.util.Arrays;

/**
 * Indicates error in finding a GSM to deploy to.
 *
 * @author kimchy
 */
public class GSMNotFoundException extends NestedRuntimeException {

    private String[] groups;

    private long timeout;

    public GSMNotFoundException(String[] groups, long timeout, Exception e) {
        super("Failed to find GSM with groups " + Arrays.asList(groups) + " and timeout [" + timeout + "]", e);
        this.groups = groups;
        this.timeout = timeout;
    }

    public GSMNotFoundException(String[] groups, long timeout) {
        super("Failed to find GSM with groups " + Arrays.asList(groups) + " and timeout [" + timeout + "]");
        this.groups = groups;
        this.timeout = timeout;
    }

    public String[] getGroups() {
        return groups;
    }

    public long getTimeout() {
        return timeout;
    }
}
