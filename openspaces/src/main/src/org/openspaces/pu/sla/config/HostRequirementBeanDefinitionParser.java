package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.requirement.HostRequirement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.HostRequirement}.
 *
 * @author kimchy
 */
public class HostRequirementBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<HostRequirement> getBeanClass(Element element) {
        return HostRequirement.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String ip = element.getAttribute("ip");
        builder.addPropertyValue("ip", ip);
    }
}