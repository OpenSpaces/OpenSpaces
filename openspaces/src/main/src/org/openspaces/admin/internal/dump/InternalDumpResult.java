package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpProvider;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.DumpProviderGridComponent;

import java.io.File;

/**
 * @author kimchy
 */
public class InternalDumpResult implements DumpProviderGridComponent.DumpResult {

    private final InternalDumpProvider dumpProvider;

    private final com.gigaspaces.internal.dump.InternalDumpResult internalResult;

    public InternalDumpResult(InternalDumpProvider dumpProvider, com.gigaspaces.internal.dump.InternalDumpResult internalResult) {
        this.dumpProvider = dumpProvider;
        this.internalResult = internalResult;
    }

    public String getName() {
        return internalResult.getName();
    }

    public void download(File targetDirectory, String fileName) throws AdminException {
        try {
            internalResult.download(dumpProvider, targetDirectory, fileName);
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
