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

package org.openspaces.admin.esm;

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.concurrent.TimeUnit;

/**
 * An Elastic Service Manager
 * @author Moran Avigdor
 */
public interface ElasticServiceManager extends AgentGridComponent, LogProviderGridComponent, DumpProvider {

    /**
     * Deploys an 'elastic' deployment based on the deployment information and the available resources.
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ElasticDeployment deployment);
    
    /**
     * Deploys an 'elastic' deployment based on the deployment information and the available resources.
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     */
    ProcessingUnit deploy(ElasticDeployment deployment, long timeout, TimeUnit timeUnit);
}
