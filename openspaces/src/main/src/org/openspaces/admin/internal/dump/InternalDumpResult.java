package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpProvider;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.dump.DumpDownloadListener;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * @author kimchy
 */
public class InternalDumpResult implements DumpResult {

    private final DumpProvider dumpProvider;

    private final InternalDumpProvider internalDumpProvider;

    private final com.gigaspaces.internal.dump.InternalDumpResult internalResult;

    public InternalDumpResult(DumpProvider dumpProvider, InternalDumpProvider internalDumpProvider, com.gigaspaces.internal.dump.InternalDumpResult internalResult) {
        this.dumpProvider = dumpProvider;
        this.internalDumpProvider = internalDumpProvider;
        this.internalResult = internalResult;
    }

    public String getName() {
        return internalResult.getName();
    }

    public long downloadSize() {
        return internalResult.downloadSize();
    }

    public void download(ZipOutputStream zos, DumpDownloadListener listener) throws IOException {
        internalResult.download(internalDumpProvider, zos, new InternalDumpDownloadListenerAdapter(dumpProvider, listener));
    }

    public void download(File target, DumpDownloadListener listener) throws AdminException {
        try {
            internalResult.download(internalDumpProvider, target, new InternalDumpDownloadListenerAdapter(dumpProvider, listener));
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }

    public void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException {
        try {
            internalResult.download(internalDumpProvider, targetDirectory, fileName, new InternalDumpDownloadListenerAdapter(dumpProvider, listener));
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
