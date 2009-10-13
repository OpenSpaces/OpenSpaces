package org.openspaces.admin;

import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;

import java.util.Map;

/**
 * @author kimchy
 */
public interface DumpProviderGridComponent extends GridComponent {

    DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException;

    DumpResult generateDump(String cause, Map<String, Object> context, String... contributors) throws AdminException;
    
    interface DumpResult {
        String getFileLocation();
    }
}