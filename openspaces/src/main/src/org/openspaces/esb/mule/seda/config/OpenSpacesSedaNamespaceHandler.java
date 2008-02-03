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

package org.openspaces.esb.mule.seda.config;

import org.mule.config.spring.MuleHierarchicalBeanDefinitionParserDelegate;
import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.delegate.InheritDefinitionParser;
import org.mule.config.spring.parsers.generic.NamedDefinitionParser;
import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;
import org.mule.config.spring.parsers.specific.ServiceDefinitionParser;
import org.openspaces.esb.mule.seda.OpenSpacesSedaModel;
import org.openspaces.esb.mule.seda.OpenSpacesSedaService;
import org.openspaces.esb.mule.seda.SpaceAwareSedaService;

/**
 * @author kimchy
 */
//TODO for now, this does not work since Mule won't parse child elements (this means that there is no current support for custom values)
public class OpenSpacesSedaNamespaceHandler extends AbstractMuleNamespaceHandler {

    public void init() {
        InheritDefinitionParser modelParser = new InheritDefinitionParser(new OrphanDefinitionParser(OpenSpacesSedaModel.class, true), new NamedDefinitionParser());
        modelParser.addBeanFlag(MuleHierarchicalBeanDefinitionParserDelegate.MULE_FORCE_RECURSE);
        registerBeanDefinitionParser("model", modelParser);

        ServiceDefinitionParser serviceDefinitionParser = new ServiceDefinitionParser(OpenSpacesSedaService.class);
        serviceDefinitionParser.addBeanFlag(MuleHierarchicalBeanDefinitionParserDelegate.MULE_FORCE_RECURSE);
        registerBeanDefinitionParser("service", serviceDefinitionParser);

        serviceDefinitionParser = new ServiceDefinitionParser(SpaceAwareSedaService.class);
        serviceDefinitionParser.addBeanFlag(MuleHierarchicalBeanDefinitionParserDelegate.MULE_FORCE_RECURSE);
        registerBeanDefinitionParser("space-aware-service", new ServiceDefinitionParser(SpaceAwareSedaService.class));
    }
}