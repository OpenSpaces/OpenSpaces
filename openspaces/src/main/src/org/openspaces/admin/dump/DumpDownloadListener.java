package org.openspaces.admin.dump;

/**
 * @author kimchy
 */
public interface DumpDownloadListener {

    void onDownload(long downloadedBytes, String name, String fileName);
}
