package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;

import java.io.File;

/**
 * @author kimchy
*/
public interface DumpResult {

    String getName();

    long downloadSize();

    void download(File target, DumpDownloadListener listener) throws AdminException;

    void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException;
}
