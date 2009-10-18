package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpProvider;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.dump.DumpDownloadListener;

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

    public long downloadSize() {
        return internalResult.downloadSize();
    }

    public void download(ZipOutputStream zos, DumpDownloadListener listener) throws IOException {
        internalResult.download(dumpProvider, zos, new InternalDumpDownloadListenerAdapter(listener));
    }

    public void download(File target, DumpDownloadListener listener) throws AdminException {
        try {
            internalResult.download(dumpProvider, target, new InternalDumpDownloadListenerAdapter(listener));
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }

    public void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException {
        try {
            internalResult.download(dumpProvider, targetDirectory, fileName, new InternalDumpDownloadListenerAdapter(listener));
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
