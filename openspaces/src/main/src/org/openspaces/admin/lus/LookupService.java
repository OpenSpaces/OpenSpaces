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

package org.openspaces.admin.lus;

import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;

/**
 * A lookup service acts a lookup server where different grid components register and maintain
 * a lease against. It provides the ability to discover these grid components.
 *
 * @author kimchy
 */
public interface LookupService extends AgentGridComponent, LogProviderGridComponent, DumpProvider {

    String[] getLookupGroups();

    LookupLocator getLookupLocator();
    
    /**
     * @return true if the lus is in admin.getLookupServices()
     */
    boolean isRunning();
}
