package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.monitor.BeanPropertyMonitor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.monitor.BeanPropertyMonitor}.
 *
 * @author kimchy
 */
public class BeanPropertyMonitorBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<BeanPropertyMonitor> getBeanClass(Element element) {
        return BeanPropertyMonitor.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String name = element.getAttribute("name");
        builder.addPropertyValue("name", name);

        String beanRef = element.getAttribute("bean-ref");
        builder.addPropertyValue("ref", beanRef);

        String propertyName = element.getAttribute("property-name");
        builder.addPropertyValue("propertyName", propertyName);

        String period = element.getAttribute("period");
        if (StringUtils.hasLength(period)) {
            builder.addPropertyValue("period", period);
        }
        String historySize = element.getAttribute("history-size");
        if (StringUtils.hasLength(historySize)) {
            builder.addPropertyValue("historySize", historySize);
        }
    }
}