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

package org.openspaces.pu.container.jee;

import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.ManifestClasspathAwareProcessingUnitContainerProvider;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;

/**
 * An extension to the {@link org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider}
 * that can handle JEE processing units.
 *
 * @author kimchy
 */
public interface JeeProcessingUnitContainerProvider extends 
    ApplicationContextProcessingUnitContainerProvider, 
    DeployableProcessingUnitContainerProvider,
    ManifestClasspathAwareProcessingUnitContainerProvider {

    /**
     * The {@link javax.servlet.ServletContext} key under which the {@link org.openspaces.core.cluster.ClusterInfo}
     * is stored.
     */
    public static final String CLUSTER_INFO_CONTEXT = "clusterInfo";

    /**
     * The {@link javax.servlet.ServletContext} key under which the {@link org.openspaces.core.properties.BeanLevelProperties}
     * is stored.
     */
    public static final String BEAN_LEVEL_PROPERTIES_CONTEXT = "beanLevelProperties";

    /**
     * The {@link javax.servlet.ServletContext} key under which the {@link org.springframework.context.ApplicationContext}
     * (loaded from the <code>pu.xml</code>) is stored.
     */
    public static final String APPLICATION_CONTEXT_CONTEXT = "applicationContext";
}
