package org.openspaces.admin;

import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;

/**
 * @author kimchy
 */
public interface LogProviderGridComponent extends GridComponent {

    /**
     * Returns the log entries matching the given matcher for the specific grid component. If it is
     * managed by a {@link org.openspaces.admin.gsa.GridServiceAgent}, will get the log through it
     * instead of creating a load on the actual grid component.
     */
    LogEntries logEntries(LogEntryMatcher matcher) throws AdminException;

    /**
     * Same as {@link #logEntries(com.gigaspaces.log.LogEntryMatcher)}, but does not try and get the logs
     * from the agent.
     */
    LogEntries logEntriesDirect(LogEntryMatcher matcher) throws AdminException;
}
