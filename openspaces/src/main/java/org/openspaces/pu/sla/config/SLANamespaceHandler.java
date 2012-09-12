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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A namespace handler for <code>sla</code> namespace.
 *
 * @author kimchy
 */
public class SLANamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("sla", new SLABeanDefinitionParser());
        registerBeanDefinitionParser("relocation-policy", new RelocationPolicyBeanDefinitionParser());
        registerBeanDefinitionParser("scale-up-policy", new ScaleUpPolicyBeanDefinitionParser());
        registerBeanDefinitionParser("bean-property-monitor", new BeanPropertyMonitorBeanDefinitionParser());
        registerBeanDefinitionParser("host", new HostRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("zone", new ZoneRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("cpu", new CpuRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("memory", new MemoryBeanDefinitionParser());
        registerBeanDefinitionParser("range", new RangeRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("system", new SystemRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("instance-SLA", new InstanceSLABeanDefinitionParser());
        registerBeanDefinitionParser("member-alive-indicator", new MemberAliveIndicatorBeanDefinitionParser());
    }
}