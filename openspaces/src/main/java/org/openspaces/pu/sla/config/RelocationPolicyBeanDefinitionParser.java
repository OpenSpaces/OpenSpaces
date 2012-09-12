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

import org.openspaces.pu.sla.RelocationPolicy;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.RelocationPolicy}.
 *
 * @author kimchy
 */
public class RelocationPolicyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<RelocationPolicy> getBeanClass(Element element) {
        return RelocationPolicy.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String monitor = element.getAttribute("monitor");
        builder.addPropertyValue("monitor", monitor);
        String low = element.getAttribute("low");
        if (StringUtils.hasLength(low)) {
            builder.addPropertyValue("low", low);
        }
        String high = element.getAttribute("high");
        if (StringUtils.hasLength(high)) {
            builder.addPropertyValue("high", high);
        }
        String lowerDampener = element.getAttribute("lower-dampener");
        if (StringUtils.hasLength(lowerDampener)) {
            builder.addPropertyValue("lowerDampener", lowerDampener);
        }
        String upperDampener = element.getAttribute("upper-dampener");
        if (StringUtils.hasLength(upperDampener)) {
            builder.addPropertyValue("upperDampener", upperDampener);
        }
    }
}