package org.openspaces.admin;

import com.gigaspaces.log.LogEntry;
import com.gigaspaces.log.LogEntryMatcher;

/**
 * @author kimchy
 */
public interface LogProviderGridComponent extends GridComponent {

    LogEntry[] log(LogEntryMatcher matcher) throws AdminException;
}
