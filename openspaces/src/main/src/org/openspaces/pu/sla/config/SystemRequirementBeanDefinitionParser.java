package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.SystemRequirement}.
 *
 * @author kimchy
 */
public class SystemRequirementBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return SystemRequirement.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String name = element.getAttribute("name");
        if (StringUtils.hasLength(name)) {
            builder.addPropertyValue("name", name);
        }
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        Element attributesEle = DomUtils.getChildElementByTagName(element, "attributes");
        if (attributesEle != null) {
            Map attributes = parserContext.getDelegate().parseMapElement(attributesEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("attributes", attributes);
        }
    }
}