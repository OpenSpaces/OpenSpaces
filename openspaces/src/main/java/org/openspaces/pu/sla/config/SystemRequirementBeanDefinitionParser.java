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

import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.SystemRequirement}.
 *
 * @author kimchy
 */
public class SystemRequirementBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return SystemRequirement.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String name = element.getAttribute("name");
        if (StringUtils.hasLength(name)) {
            builder.addPropertyValue("name", name);
        }
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        Element attributesEle = DomUtils.getChildElementByTagName(element, "attributes");
        if (attributesEle != null) {
            Map attributes = parserContext.getDelegate().parseMapElement(attributesEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("attributes", attributes);
        }
    }
}