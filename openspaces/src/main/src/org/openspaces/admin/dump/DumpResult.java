package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;

import java.io.File;

/**
 * A results of a dump operation. Allows to download the dump into a specific file.
 *
 * @author kimchy
 * @see org.openspaces.admin.dump.DumpProvider
*/
public interface DumpResult {

    /**
     * The name of the dump.
     */
    String getName();

    /**
     * The download size of the dump.
     */
    long downloadSize();

    /**
     * Downloads the dump into the target file. Allowing to provide a listener to track the downloading process.
     */
    void download(File target, DumpDownloadListener listener) throws AdminException;

    /**
     * Downloads the dump into the target file. Allowing to provide a listener to track the downloading process.
     */
    void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException;
}
