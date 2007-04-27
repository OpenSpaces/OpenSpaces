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
        registerBeanDefinitionParser("cpu", new CpuRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("memory", new MemoryBeanDefinitionParser());
        registerBeanDefinitionParser("range", new RangeRequirementBeanDefinitionParser());
        registerBeanDefinitionParser("system", new SystemRequirementBeanDefinitionParser());
    }
}