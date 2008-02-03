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

package org.openspaces.esb.mule;

import org.mule.api.MuleException;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.MessageDispatcher;
import org.mule.transport.AbstractMessageDispatcherFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates new instances of {@link OpenSpacesMessageDispatcher} for each unique endpoint.
 * 
 * @author yitzhaki
 */
public class OpenSpacesMessageDispatcherFactory extends AbstractMessageDispatcherFactory {
    
    private Map<ImmutableEndpoint, OpenSpacesMessageDispatcher> dispatcherMap =
            new ConcurrentHashMap<ImmutableEndpoint, OpenSpacesMessageDispatcher>();


    public MessageDispatcher create(ImmutableEndpoint endpoint) throws MuleException {
        OpenSpacesMessageDispatcher spacesMessageDispatcher = dispatcherMap.get(endpoint);
        if (spacesMessageDispatcher == null) {
            spacesMessageDispatcher = new OpenSpacesMessageDispatcher(endpoint);
            dispatcherMap.put(endpoint, spacesMessageDispatcher);
        }
        return spacesMessageDispatcher;
    }
}
