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

package org.openspaces.esb.mule.eventcontainer.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.generic.MuleOrphanDefinitionParser;
import org.openspaces.esb.mule.eventcontainer.OpenSpacesConnector;

/**
 * A namespace handler for <code>OpenSpaces</code> namespace.
 *
 * @author yitzhaki
 */
public class OpenSpacesEventContainerNamespaceHandler extends AbstractMuleNamespaceHandler {

    public void init() {
        registerStandardTransportEndpoints(OpenSpacesConnector.OS_EVENT_CONTAINER, new String[]{});
        registerBeanDefinitionParser("connector", new MuleOrphanDefinitionParser(OpenSpacesConnector.class, true));
    }
}
