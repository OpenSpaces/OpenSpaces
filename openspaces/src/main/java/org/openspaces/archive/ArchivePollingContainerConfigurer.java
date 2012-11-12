/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.archive;

import org.openspaces.core.GigaSpace;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A configuration for {@link org.openspaces.archive.ArchivePollingContainer} using fluent API.
 *
 * <p>Sample usage of static template:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurerPrimary = new UrlSpaceConfigurer("/./space");
 * GigaSpace gigaSpace = new GigaSpaceConfigurer(urlSpaceConfigurerPrimary.space()).gigaSpace();
 * ArchivePollingEventListenerContainer archiveContainer = new ArchiveContainerConfigurer(gigaSpace)
 *              .template(new TestMessage())
 *              .archiveHandler(new CasandraArchiveOperationHandler())
 *              .create();
 *
 * ...
 *
 * archiveContainer.destroy();
 * urlSpaceConfigurerPrimary.destroy();
 * </pre>
 *
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class ArchivePollingContainerConfigurer {

    private ArchivePollingContainer archiveContainer;
    private boolean initialized = false;
    
    public ArchivePollingContainerConfigurer(GigaSpace gigaSpace) {
        archiveContainer = new ArchivePollingContainer();
        archiveContainer.setGigaSpace(gigaSpace);
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setBeanName(String)
     */
    public ArchivePollingContainerConfigurer name(String name) {
        archiveContainer.setBeanName(name);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setConcurrentConsumers(int)
     */
    public ArchivePollingContainerConfigurer concurrentConsumers(int concurrentConsumers) {
        archiveContainer.setConcurrentConsumers(concurrentConsumers);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setTemplate(Object)
     */
    public ArchivePollingContainerConfigurer template(Object template) {
        archiveContainer.setTemplate(template);
        return this;
    }

    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setMaxConcurrentConsumers(int)
     */
    public ArchivePollingContainerConfigurer maxConcurrentConsumers(int maxConcurrentConsumers) {
        archiveContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setReceiveTimeout(long)
     */
    public ArchivePollingContainerConfigurer receiveTimeout(long receiveTimeout) {
        archiveContainer.setReceiveTimeout(receiveTimeout);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setPerformSnapshot(boolean)
     */
    public ArchivePollingContainerConfigurer performSnapshot(boolean performSnapshot) {
        archiveContainer.setPerformSnapshot(performSnapshot);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setRecoveryInterval(long)
     */
    public ArchivePollingContainerConfigurer recoveryInterval(long recoveryInterval) {
        archiveContainer.setRecoveryInterval(recoveryInterval);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setAutoStart(boolean)
     */
    public ArchivePollingContainerConfigurer autoStart(boolean autoStart) {
        archiveContainer.setAutoStart(autoStart);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setArchiveHandler(ArchiveOperationHandler)
     */
    public ArchivePollingContainerConfigurer archiveHandler(ArchiveOperationHandler archiveHandler) {
        archiveContainer.setArchiveHandler(archiveHandler);
        return this;
    }
    
    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setTransactionManager(org.springframework.transaction.PlatformTransactionManager)
     */
    public ArchivePollingContainerConfigurer transactionManager(PlatformTransactionManager transactionManager) {
        archiveContainer.setTransactionManager(transactionManager);
        return this;
    }

    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setTransactionName(String)
     */
    public ArchivePollingContainerConfigurer transactionName(String transactionName) {
        archiveContainer.setTransactionName(transactionName);
        return this;
    }

    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setTransactionTimeout(int)
     */
    public ArchivePollingContainerConfigurer transactionTimeout(int transactionTimeout) {
        archiveContainer.setTransactionTimeout(transactionTimeout);
        return this;
    }

    /**
     * @see org.openspaces.archive.ArchivePollingContainer#setTransactionIsolationLevel(int)
     */
    public ArchivePollingContainerConfigurer transactionIsolationLevel(int transactionIsolationLevel) {
        archiveContainer.setTransactionIsolationLevel(transactionIsolationLevel);
        return this;
    }

    public ArchivePollingContainer create() {
        if (!initialized) {
            archiveContainer.setRegisterSpaceModeListener(true);
            archiveContainer.afterPropertiesSet();
            initialized = true;
        }
        return archiveContainer;
    }


    /**
     * @see ArchivePollingContainer#setArchiveHandlerProvider(Object)
     */
    public ArchivePollingContainerConfigurer archiveHandlerProvider(Object archiveHandlerProvider) {
        archiveContainer.setArchiveHandlerProvider(archiveHandlerProvider);
        return this;
    }

    
}
