package org.openspaces.remoting.config;

import org.openspaces.remoting.SpaceRemotingProxyFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link SpaceRemotingProxyFactoryBean}.
 * 
 * @author kimchy
 */
public class ProxyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String GIGA_SPACE = "giga-space";

    private static final String INTERFACE = "interface";

    private static final String VOID_ONE_WAY = "void-one-way";

    private static final String GLOBAL_ONE_WAY = "global-one-way";

    private static final String TIMEOUT = "timeout";

    private static final String ROUTING_HANDLER = "routing-handler";

    protected Class<SpaceRemotingProxyFactoryBean> getBeanClass(Element element) {
        return SpaceRemotingProxyFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String gigaSpace = element.getAttribute(GIGA_SPACE);
        builder.addPropertyReference("gigaSpace", gigaSpace);

        String inter = element.getAttribute(INTERFACE);
        builder.addPropertyValue("serviceInterface", inter);

        String voidOneWay = element.getAttribute(VOID_ONE_WAY);
        if (StringUtils.hasLength(voidOneWay)) {
            builder.addPropertyValue("voidOneWay", voidOneWay);
        }

        String globalOneWay = element.getAttribute(GLOBAL_ONE_WAY);
        if (StringUtils.hasLength(globalOneWay)) {
            builder.addPropertyValue("globalOneWay", globalOneWay);
        }

        String timeout = element.getAttribute(TIMEOUT);
        if (StringUtils.hasLength(timeout)) {
            builder.addPropertyValue("timeout", timeout);
        }

        Element routingHandlerEle = DomUtils.getChildElementByTagName(element, ROUTING_HANDLER);
        if (routingHandlerEle != null) {
            builder.addPropertyValue("remoteRoutingHandler", parserContext.getDelegate().parsePropertyValue(routingHandlerEle, builder.getRawBeanDefinition(), "remoteRoutingHandler"));
        }

    }
}