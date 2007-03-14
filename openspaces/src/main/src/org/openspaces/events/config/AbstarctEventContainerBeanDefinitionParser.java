package org.openspaces.events.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public abstract class AbstarctEventContainerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String LISTENER = "listener";

    private static final String GIGA_SPACE = "giga-space";

    private static final String ACTIVE_WHEN_PRIMARY = "active-when-primary";

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        Element listenerEle = DomUtils.getChildElementByTagName(element, LISTENER);
        builder.addPropertyValue("eventListener", parserContext.getDelegate().parsePropertyValue(listenerEle,
                builder.getRawBeanDefinition(), "eventListener"));

        String gigaSpace = element.getAttribute(GIGA_SPACE);
        builder.addPropertyReference("gigaSpace", gigaSpace);

        String activeWhenPrimary = element.getAttribute(ACTIVE_WHEN_PRIMARY);
        if (StringUtils.hasLength(activeWhenPrimary)) {
            builder.addPropertyValue("activeWhenPrimary", Boolean.valueOf(activeWhenPrimary));
        }
    }
}
