package org.openspaces.events.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public abstract class AbstractResultEventAdapterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String WRITE_LEASE = "write-lease";

    private static final String UPDATE_OR_WRITE = "update-or-write";

    private static final String UPDATE_TIMEOUT = "update-timeout";

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String writeLease = element.getAttribute(WRITE_LEASE);
        if (StringUtils.hasLength(writeLease)) {
            builder.addPropertyValue("writeLease", Boolean.valueOf(writeLease));
        }

        String updateOrWrite = element.getAttribute(UPDATE_OR_WRITE);
        if (StringUtils.hasLength(updateOrWrite)) {
            builder.addPropertyValue("updateOrWrite", Boolean.valueOf(updateOrWrite));
        }

        String updateTimeout = element.getAttribute(UPDATE_TIMEOUT);
        if (StringUtils.hasLength(updateTimeout)) {
            builder.addPropertyValue("updateTimeout", Boolean.valueOf(updateTimeout));
        }

    }
}
