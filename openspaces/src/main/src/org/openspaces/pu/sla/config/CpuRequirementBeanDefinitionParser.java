package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.requirement.CpuRequirement;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.CpuRequirement}.
 *
 * @author kimchy
 */
public class CpuRequirementBeanDefinitionParser extends RangeRequirementBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return CpuRequirement.class;
    }
}