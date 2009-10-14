package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;

import java.io.File;

/**
 * @author kimchy
*/
public interface DumpResult {

    String getName();

    void download(File targetDirectory, String fileName) throws AdminException;
}
