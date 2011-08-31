package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpDownloadListener;
import org.openspaces.admin.dump.DumpDownloadListener;
import org.openspaces.admin.dump.DumpProvider;

/**
 * @author kimchy
 */
public class InternalDumpDownloadListenerAdapter implements InternalDumpDownloadListener {

    private final DumpProvider dumpProvider;

    private final DumpDownloadListener listener;

    public InternalDumpDownloadListenerAdapter(DumpProvider dumpProvider, DumpDownloadListener listener) {
        this.dumpProvider = dumpProvider;
        this.listener = listener;
    }

    @Override
    public void onDownload(long downloadedBytes, String name, String fileName) {
        if (listener != null) {
            listener.onDownload(dumpProvider, downloadedBytes, name, fileName);
        }
    }
}
