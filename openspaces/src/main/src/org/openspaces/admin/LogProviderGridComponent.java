/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin;

import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;

/**
 * A grid component that can provide the ability to extract logging information.
 *
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
