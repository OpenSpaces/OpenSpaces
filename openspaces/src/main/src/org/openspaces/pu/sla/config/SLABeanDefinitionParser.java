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

import org.openspaces.pu.sla.SLA;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.SLA}.
 *
 * @author kimchy
 */
public class SLABeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<SLA> getBeanClass(Element element) {
        return SLA.class;
    }

    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
        // there can be only one SLA bean within a processing unit application context, and its name must be SLA
        return "SLA";
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String numberOfInstnaces = element.getAttribute("number-of-instances");
        if (StringUtils.hasLength(numberOfInstnaces)) {
            builder.addPropertyValue("numberOfInstances", numberOfInstnaces);
        }

        String numberOfBackups = element.getAttribute("number-of-backups");
        if (StringUtils.hasLength(numberOfBackups)) {
            builder.addPropertyValue("numberOfBackups", numberOfBackups);
        }

        String clusterSchema = element.getAttribute("cluster-schema");
        if (StringUtils.hasLength(clusterSchema)) {
            builder.addPropertyValue("clusterSchema", clusterSchema);
        }

        String maxInstancesPerVm = element.getAttribute("max-instances-per-vm");
        if (StringUtils.hasLength(maxInstancesPerVm)) {
            builder.addPropertyValue("maxInstancesPerVM", maxInstancesPerVm);
        }
        String maxInstancesPerMachine = element.getAttribute("max-instances-per-machine");
        if (StringUtils.hasLength(maxInstancesPerMachine)) {
            builder.addPropertyValue("maxInstancesPerMachine", maxInstancesPerMachine);
        }
        String maxInstancesPerZone = element.getAttribute("max-instances-per-zone");
        if (StringUtils.hasLength(maxInstancesPerZone)) {
            builder.addPropertyValue("maxInstancesPerZoneAsString", maxInstancesPerZone);
        }
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        // policy
        Element scaleUpPolicyEle = DomUtils.getChildElementByTagName(element, "scale-up-policy");
        if (scaleUpPolicyEle != null) {
            builder.addPropertyValue("policy", parserContext.getDelegate().parsePropertySubElement(scaleUpPolicyEle, builder.getRawBeanDefinition()));
        }
        Element relocationPolicyEle = DomUtils.getChildElementByTagName(element, "relocation-policy");
        if (relocationPolicyEle != null) {
            builder.addPropertyValue("policy", parserContext.getDelegate().parsePropertySubElement(relocationPolicyEle, builder.getRawBeanDefinition()));
        }

        Element memberAliveIndicatorEle = DomUtils.getChildElementByTagName(element, "member-alive-indicator");
        if (memberAliveIndicatorEle != null) {
            builder.addPropertyValue("memberAliveIndicator", parserContext.getDelegate().parsePropertySubElement(memberAliveIndicatorEle, builder.getRawBeanDefinition()));
        }

        // montiors
        Element monitorsEle = DomUtils.getChildElementByTagName(element, "monitors");
        if (monitorsEle != null) {
            List monitors = parserContext.getDelegate().parseListElement(monitorsEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("monitors", monitors);
        }

        // requirements
        Element requirementsEle = DomUtils.getChildElementByTagName(element, "requirements");
        if (requirementsEle != null) {
            List requirements = parserContext.getDelegate().parseListElement(requirementsEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("requirements", requirements);
        }

        Element instanceSLAsEle = DomUtils.getChildElementByTagName(element, "instance-SLAs");
        if (instanceSLAsEle != null) {
            List instanceSLAs = parserContext.getDelegate().parseListElement(instanceSLAsEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("instanceSLAs", instanceSLAs);
        }
    }
}