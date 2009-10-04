package org.openspaces.admin;

import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;

/**
 * @author kimchy
 */
public interface LogProviderGridComponent extends GridComponent {

    LogEntries log(LogEntryMatcher matcher) throws AdminException;
}
