package org.openspaces.admin;

import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;

import java.util.Map;
import java.io.File;

/**
 * @author kimchy
 */
public interface DumpProviderGridComponent extends GridComponent {

    DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException;

    DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException;
    
    interface DumpResult {

        String getName();

        void download(File targetDirectory, String fileName) throws AdminException;
    }
}