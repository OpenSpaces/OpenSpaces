/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.events.support;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.DynamicEventTemplateProvider;
import org.openspaces.events.adapter.AnnotationDynamicEventTemplateProviderAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author kimchy
 */
public class AnnotationProcessorUtils {

    private AnnotationProcessorUtils() {

    }

    public static EventContainersBus findBus(ApplicationContext applicationContext) {
        
        EventContainersBus ecb = null;
        Map<String, EventContainersBus> eventContainerBuses = applicationContext.getBeansOfType(EventContainersBus.class);
        
        //resolve <os-events:annotation-support>
        ecb = eventContainerBuses.get(org.openspaces.events.config.AnnotationSupportBeanDefinitionParser.PRIMARY_EVENT_CONTAINER_BUS_BEAN_NAME);
        if (ecb == null) {
            //resolve <os-archive:annotation-support>
            ecb = eventContainerBuses.get(org.openspaces.archive.config.AnnotationSupportBeanDefinitionParser.SECONDARY_EVENT_CONTAINER_BUS_BEAN_NAME);
        }
        if (ecb == null) {
            throw new IllegalArgumentException("No event container bus found, can't register polling container. Add <os-events:annotation-support> or <os-archive:annotation-support>");
        }
        return ecb;
    }

    public static GigaSpace findGigaSpace(final Object bean, String gigaSpaceName, ApplicationContext applicationContext, String beanName) throws IllegalArgumentException {
        GigaSpace gigaSpace = null;
        if (StringUtils.hasLength(gigaSpaceName)) {
            gigaSpace = (GigaSpace) applicationContext.getBean(gigaSpaceName);
            if (gigaSpace == null) {
                throw new IllegalArgumentException("Failed to lookup GigaSpace using name [" + gigaSpaceName + "] in bean [" + beanName + "]");
            }
            return gigaSpace;
        }

        Map gigaSpaces = applicationContext.getBeansOfType(GigaSpace.class);
        if (gigaSpaces.size() == 1) {
            return (GigaSpace) gigaSpaces.values().iterator().next();
        } 
        throw new IllegalArgumentException("Failed to resolve GigaSpace to use with event container, " +
                "[" + beanName + "] does not specifiy one, has no fields of that type, and there are more than one GigaSpace beans within the context");
    }

    public static PlatformTransactionManager findTxManager(String txManagerName, ApplicationContext applicationContext, String beanName) {
        PlatformTransactionManager txManager;
        if (StringUtils.hasLength(txManagerName)) {
            txManager = (PlatformTransactionManager) applicationContext.getBean(txManagerName);
            if (txManager == null) {
                throw new IllegalArgumentException("Failed to find transaction manager for name [" + txManagerName + "] in bean [" + beanName + "]");
            }
        } else {
            Map txManagers = applicationContext.getBeansOfType(PlatformTransactionManager.class);
            if (txManagers.size() == 1) {
                txManager = (PlatformTransactionManager) txManagers.values().iterator().next();
            } else {
                throw new IllegalArgumentException("More than one transaction manager defined in context, and no transactionManager specified, can't detect one");
            }
        }
        return txManager;
    }


    /**
     * @return a {@link DynamicEventTemplateProvider} based on interface or annotation.
     */
    public static DynamicEventTemplateProvider findDynamicEventTemplateProvider(final Object bean) {
        
        DynamicEventTemplateProvider provider = null;
        if (bean instanceof DynamicEventTemplateProvider) {
            provider =(DynamicEventTemplateProvider) bean;
        } 
        else {
            AnnotationDynamicEventTemplateProviderAdapter adapter = 
                    new AnnotationDynamicEventTemplateProviderAdapter();
            adapter.setDelegate(bean);
            adapter.setFailSilentlyIfMethodNotFound(true);
            adapter.afterPropertiesSet();
            if (adapter.isMethodFound()) {
                provider = adapter;
            }
        }
        return provider;
    }
}
