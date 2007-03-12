package org.openspaces.jdbc.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author kimchy
 */
public class JdbcNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("driver-data-source", new DataSourceBeanDefinitionParser());
    }
}