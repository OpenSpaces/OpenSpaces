package org.openspaces.admin.dump;

/**
 * A dump download listener allowing to track the downloading of dump result.
 *
 * @author kimchy
 * @see org.openspaces.admin.dump.DumpResult
 */
public interface DumpDownloadListener {

    /**
     * Called repeatedly with the downloaded bytes since the last call (the total can be obtained from
     * {@link DumpResult#downloadSize()}.
     */
    void onDownload(DumpProvider dumpProvider, long downloadedBytes, String name, String fileName);
}
