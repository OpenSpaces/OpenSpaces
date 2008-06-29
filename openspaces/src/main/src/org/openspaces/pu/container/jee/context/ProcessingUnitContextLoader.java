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

package org.openspaces.pu.container.jee.context;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author kimchy
 */
public class ProcessingUnitContextLoader extends ContextLoader {

    protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException {
        return (ApplicationContext) servletContext.getAttribute(JeeProcessingUnitContainerProvider.APPLICATION_CONTEXT_CONTEXT);
    }

    protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent) throws BeansException {
        ProcessingUnitWebApplicationContext wac = new ProcessingUnitWebApplicationContext();

        ClusterInfo clusterInfo = (ClusterInfo) servletContext.getAttribute(JeeProcessingUnitContainerProvider.CLUSTER_INFO_CONTEXT);
        
        BeanLevelProperties beanLevelProperties = (BeanLevelProperties) servletContext.getAttribute(JeeProcessingUnitContainerProvider.BEAN_LEVEL_PROPERTIES_CONTEXT);
        if (beanLevelProperties != null) {
            wac.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties, clusterInfo));
            wac.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }

        if (clusterInfo != null) {
            wac.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        wac.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));

        wac.setParent(parent);
        wac.setServletContext(servletContext);
        wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
        customizeContext(servletContext, wac);
        wac.refresh();
        return wac;

    }
}
