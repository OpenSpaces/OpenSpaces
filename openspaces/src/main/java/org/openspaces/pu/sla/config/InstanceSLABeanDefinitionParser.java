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

package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.InstanceSLA;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author kimchy
 */
public class InstanceSLABeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return InstanceSLA.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        // requirements
        Element requirementsEle = DomUtils.getChildElementByTagName(element, "requirements");
        if (requirementsEle != null) {
            List requirements = parserContext.getDelegate().parseListElement(requirementsEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("requirements", requirements);
        }
    }
}
