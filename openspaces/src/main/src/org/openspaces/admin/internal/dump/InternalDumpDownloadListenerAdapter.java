package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpDownloadListener;
import org.openspaces.admin.dump.DumpDownloadListener;

/**
 * @author kimchy
 */
public class InternalDumpDownloadListenerAdapter implements InternalDumpDownloadListener {

    private final DumpDownloadListener listener;

    public InternalDumpDownloadListenerAdapter(DumpDownloadListener listener) {
        this.listener = listener;
    }

    public void onDownload(long downloadedBytes, String name, String fileName) {
        if (listener != null) {
            listener.onDownload(downloadedBytes, name, fileName);
        }
    }
}
