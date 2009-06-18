/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.events.config;

import org.openspaces.events.asyncpolling.config.AsyncPollingAnnotationPostProcessor;
import org.openspaces.events.notify.config.NotifyAnnotationPostProcessor;
import org.openspaces.events.polling.config.PollingAnnotationPostProcessor;
import org.openspaces.events.support.EventContainersBus;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class AnnotationSupportBeanDefinitionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinition bd = new RootBeanDefinition(EventContainersBus.class);
        BeanComponentDefinition bcd = new BeanComponentDefinition(bd, "internal-eventContainerBus");
        parserContext.registerBeanComponent(bcd);

        bd = new RootBeanDefinition(PollingAnnotationPostProcessor.class);
        bcd = new BeanComponentDefinition(bd, "internal-pollingContaienrAnnotationPostProcessor");
        parserContext.registerBeanComponent(bcd);

        bd = new RootBeanDefinition(NotifyAnnotationPostProcessor.class);
        bcd = new BeanComponentDefinition(bd, "internal-notifyContaienrAnnotationPostProcessor");
        parserContext.registerBeanComponent(bcd);

        bd = new RootBeanDefinition(AsyncPollingAnnotationPostProcessor.class);
        bcd = new BeanComponentDefinition(bd, "internal-asyncPollingContaienrAnnotationPostProcessor");
        parserContext.registerBeanComponent(bcd);

        return null;
    }
}
