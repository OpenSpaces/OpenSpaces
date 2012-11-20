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
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.TransactionStatus;

/**
 * Takes objects specified in the template into the archive handler defined by {@link #setArchiveHandler(ArchiveOperationHandler)}
 * This container can be used to take (remove) objects from the Space and persist them into an external service.
 * 
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class ArchivePollingContainer 
    extends SimplePollingEventListenerContainer 
    implements SpaceDataEventListener<Object> {

    private ArchiveOperationHandler archiveHandler;
    private boolean atomicArchiveOfMultipleObjects;
    private int batchSize = 50; // == MultiTakeReceiveOperationHandler#DEFAULT_MAX_ENTRIES;
    
    public ArchivePollingContainer() {
        super.setEventListener(this);
    }
    
    @Required
    public void setArchiveHandler(ArchiveOperationHandler archiveHandler) {
       this.archiveHandler = archiveHandler;
    }
    
    
    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (archiveHandler == null) {
            throw new IllegalStateException("Archive handler cannot be null");
        }
    }
    
    
    @Override
    public void afterPropertiesSet() {
        
        initArchiveHandler();
        
        atomicArchiveOfMultipleObjects = archiveHandler.supportsBatchArchiving();
        if (atomicArchiveOfMultipleObjects) {
            MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
            receiveHandler.setMaxEntries(batchSize);
            setReceiveOperationHandler(receiveHandler);
            super.setPassArrayAsIs(true);
        }
        super.afterPropertiesSet();
    }

    /**
     * Validate there is a valid archiveHandler or extract one from a valid archiveHandlerProvider.
     */
    private void initArchiveHandler() {
                
        if (archiveHandler == null) {
            throw new IllegalStateException("archiveHandler cannot be null");
        }
    }

    @Override
    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        if (atomicArchiveOfMultipleObjects) {
            archiveHandler.archive((Object[])data);
        }
        else {
            archiveHandler.archive(data);
        }
    }

    /**
     * @param batchSize - The maximum number of objects to hand over to the archiver in one method call.
     * This parameter has affect only if the archive handler {@link ArchiveOperationHandler#supportsAtomicBatchArchiving()}
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return this.batchSize;
    }
    
}