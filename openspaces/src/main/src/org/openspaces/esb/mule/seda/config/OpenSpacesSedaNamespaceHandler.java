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

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.delegate.InheritDefinitionParser;
import org.mule.config.spring.parsers.generic.NamedDefinitionParser;
import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;
import org.openspaces.esb.mule.seda.OpenSpacesSedaModel;
import org.openspaces.esb.mule.seda.OpenSpacesSedaService;
import org.openspaces.esb.mule.seda.SpaceAwareSedaService;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author kimchy
 */
//TODO for now, this does not work since Mule won't parse child elements (this means that there is no current support for custom values)
public class OpenSpacesSedaNamespaceHandler extends AbstractMuleNamespaceHandler {

    public void init() {
        registerBeanDefinitionParser("model", new InheritDefinitionParser(new ModelOrphanDefinitionParser(OpenSpacesSedaModel.class, true), new NamedDefinitionParser()));
        registerBeanDefinitionParser("service", new ServiceDefinitionParser(OpenSpacesSedaService.class));
        registerBeanDefinitionParser("space-aware-service", new ServiceDefinitionParser(SpaceAwareSedaService.class));
    }

    private class ModelOrphanDefinitionParser extends OrphanDefinitionParser {

        public ModelOrphanDefinitionParser(boolean singleton) {
            super(singleton);
        }

        public ModelOrphanDefinitionParser(Class beanClass, boolean singleton) {
            super(beanClass, singleton);
        }

        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            super.doParse(element, parserContext, builder);
            NodeList list = element.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i) instanceof Element) {
                    Element childElement = (Element) list.item(i);
                    getParserContext().getDelegate().parseCustomElement(childElement, builder.getBeanDefinition());
                }
            }
        }

    }

    public class ServiceDefinitionParser extends ModelOrphanDefinitionParser {

        public ServiceDefinitionParser(Class clazz) {
            super(clazz, true);
        }

        //@java.lang.Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            Element parent = (Element) element.getParentNode();
            String modelName = parent.getAttribute(ATTRIBUTE_NAME);
            builder.addPropertyReference("model", modelName);
            super.doParse(element, parserContext, builder);
        }

    }

}