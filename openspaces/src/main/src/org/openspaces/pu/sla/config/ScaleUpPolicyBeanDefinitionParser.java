package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.ScaleUpPolicy;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.ScaleUpPolicy}.
 *
 * @author kimchy
 */
public class ScaleUpPolicyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<ScaleUpPolicy> getBeanClass(Element element) {
        return ScaleUpPolicy.class;
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
        String maxInstances = element.getAttribute("max-instances");
        if (StringUtils.hasLength(maxInstances)) {
            builder.addPropertyValue("scaleUpTo", maxInstances);
        }
    }
}