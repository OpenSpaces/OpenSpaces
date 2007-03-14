package org.openspaces.remoting.config;

import java.util.List;

import org.openspaces.remoting.SpaceRemotingServiceExporter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class ServiceExporterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String SERVICE = "service";

    protected Class<SpaceRemotingServiceExporter> getBeanClass(Element element) {
        return SpaceRemotingServiceExporter.class;
    }

    @SuppressWarnings("unchecked")
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        List<Element> serviceElements = DomUtils.getChildElementsByTagName(element, SERVICE);
        ManagedList list = new ManagedList(serviceElements.size());
        for (Element ele : serviceElements) {
            list.add(parserContext.getDelegate().parsePropertyValue(ele, builder.getRawBeanDefinition(), null));
        }
        builder.addPropertyValue("services", list);
    }
}