package org.openspaces.events.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author kimchy
 */
public class EventsNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("polling-container", new PollingContainerBeanDefinitionParser());
        registerBeanDefinitionParser("notify-container", new NotifyContainerBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-adapter", new AnnotationEventAdapterBeanDefinitionParser());
    }
}
