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

package org.openspaces.pu.container.spi;

import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.pu.container.ClassLoaderAwareProcessingUnitContainerProvider;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author kimchy
 */
public interface ApplicationContextProcessingUnitContainerProvider extends ProcessingUnitContainerProvider,
        ClusterInfoAware, BeanLevelPropertiesAware, ClassLoaderAwareProcessingUnitContainerProvider {

    static final String DEFAULT_PU_CONTEXT_LOCATION = "classpath*:/META-INF/spring/pu.xml";
    
    static final String DEFAULT_FS_PU_CONTEXT_LOCATION = "META-INF/spring/pu.xml";
    
    void addConfigLocation(String configLocation) throws IOException;

    void addConfigLocation(Resource resource) throws IOException;
}
