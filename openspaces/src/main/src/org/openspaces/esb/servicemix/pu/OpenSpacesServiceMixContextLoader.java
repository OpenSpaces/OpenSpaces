/*
* Copyright 2006-2007 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openspaces.esb.servicemix.pu;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * <code> OpenSpacesServiceMixContextLoader</code> used for loading Servicemix configuration that refrenced from PU configuration
 * file.
 *
 * <p>It sets the PU appliction context as the parent of Mule appliction context, giving it the ability to access beans
 * that declerd in the PU appliction context.
 *
 * @author yitzhaki
 */
public class OpenSpacesServiceMixContextLoader implements ApplicationContextAware, DisposableBean,
        ApplicationListener, BeanLevelPropertiesAware, ClusterInfoAware {

    private static final String DEFAULT_LOCATION = "/META-INF/spring/servicemix.xml";

    private String location;

    private ServiceMixApplicationContext applicationContext;

    private ApplicationContext parentApplicationContext;

    private volatile boolean contextCreated = false;

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;


    public OpenSpacesServiceMixContextLoader() {
    }

    /**
     * Used to pass the {@link BeanLevelProperties} to the newly created application context.
     */
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Used to pass {@link ClusterInfo} to the newly created application context.
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        parentApplicationContext = applicationContext;
    }


    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (this.location == null) {
            this.location = DEFAULT_LOCATION;
        }
        if (!contextCreated) {
            contextCreated = true;
            try {
                applicationContext = new ServiceMixApplicationContext(location);
                applicationContext.setParent(parentApplicationContext);
                // add config information if provided
                if (beanLevelProperties != null) {
                    applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties));
                    applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
                }
                if (clusterInfo != null) {
                    applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
                }
                applicationContext.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));
                applicationContext.refresh();
            } catch (Exception e) {
                if (applicationContext != null) {
                    applicationContext.close();
                }
                throw new RuntimeException("Failed to start ServiceMix [" + location + "]", e);
            }
        }
    }


    public void destroy() throws Exception {
        applicationContext.close();
    }
}
