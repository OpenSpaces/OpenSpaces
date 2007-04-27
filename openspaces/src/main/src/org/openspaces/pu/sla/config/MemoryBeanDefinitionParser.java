package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.requirement.MemoryRequirement;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.MemoryRequirement}.
 *
 * @author kimchy
 */
public class MemoryBeanDefinitionParser extends RangeRequirementBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return MemoryRequirement.class;
    }
}