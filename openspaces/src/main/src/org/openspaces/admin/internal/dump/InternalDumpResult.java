package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpProvider;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * @author kimchy
 */
public class InternalDumpResult implements DumpResult {

    private final InternalDumpProvider dumpProvider;

    private final com.gigaspaces.internal.dump.InternalDumpResult internalResult;

    public InternalDumpResult(InternalDumpProvider dumpProvider, com.gigaspaces.internal.dump.InternalDumpResult internalResult) {
        this.dumpProvider = dumpProvider;
        this.internalResult = internalResult;
    }

    public String getName() {
        return internalResult.getName();
    }

    public void download(ZipOutputStream zos) throws IOException {
        internalResult.download(dumpProvider, zos);
    }

    public void download(File target) throws AdminException {
        try {
            internalResult.download(dumpProvider, target);
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }

    public void download(File targetDirectory, String fileName) throws AdminException {
        try {
            internalResult.download(dumpProvider, targetDirectory, fileName);
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
