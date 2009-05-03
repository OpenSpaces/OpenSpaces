/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin;

import org.openspaces.admin.gsa.GridServiceAgent;

/**
 * An Agent grid component is a {@link org.openspaces.admin.GridComponent} that can be started by a
 * {@link org.openspaces.admin.gsa.GridServiceAgent}.
 *
 * @author kimchy
 */
public interface AgentGridComponent extends GridComponent {

    /**
     * Returns the {@link GridServiceAgent} that started the grid component.
     */
    GridServiceAgent getGridServiceAgent();

    /**
     * Kills the grid component. The Grid Service Agent will not try to start it (as it does when abnormal
     * termination of the component occurs).
     */
    void kill();

    /**
     * Restarts the grid component. Completely killing the process of the component, and then starting it
     * again.
     */
    void restart();
}
