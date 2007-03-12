package org.openspaces.remoting.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author kimchy
 */
public class RemotingNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("service-exporter", new ServiceExporterBeanDefinitionParser());
        registerBeanDefinitionParser("proxy", new ProxyBeanDefinitionParser());
    }
}