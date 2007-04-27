package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.requirement.RangeRequirement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.RangeRequirement}.
 *
 * @author kimchy
 */
public class RangeRequirementBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return RangeRequirement.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String monitor = element.getAttribute("monitor");
        if (StringUtils.hasLength(monitor)) {
            builder.addPropertyValue("monitor", monitor);
        }
        String low = element.getAttribute("low");
        if (StringUtils.hasLength(low)) {
            builder.addPropertyValue("low", low);
        }
        String high = element.getAttribute("high");
        if (StringUtils.hasLength(high)) {
            builder.addPropertyValue("high", high);
        }
    }
}