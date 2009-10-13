package org.openspaces.admin.internal.dump;

import org.openspaces.admin.DumpProviderGridComponent;

/**
 * @author kimchy
 */
public class InternalDumpResult implements DumpProviderGridComponent.DumpResult {

    private final com.gigaspaces.internal.dump.InternalDumpResult internalResult;

    public InternalDumpResult(com.gigaspaces.internal.dump.InternalDumpResult internalResult) {
        this.internalResult = internalResult;
    }

    public String getFileLocation() {
        return internalResult.getLocation();
    }
}
