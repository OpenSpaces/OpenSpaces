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
package org.openspaces.itest.archive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.openspaces.archive.ArchiveOperationHandler;
import org.springframework.beans.factory.annotation.Required;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author Itai Frenkel
 * @since 9.1.1
 * 
 */
public class MockArchiveOperationsHandler implements ArchiveOperationHandler {

    private boolean atomic;
    private final Collection<Object[]> archivedObjectsOperations = new ConcurrentLinkedQueue<Object[]>();
    
    @Override
    public void archive(Object... objects) { 
        boolean added = archivedObjectsOperations.add(objects);
        if (!added) {
            throw new IllegalStateException("Failed to add objects: " + Arrays.toString(objects));
        }
    }

    @Override
    public boolean supportsAtomicBatchArchiving() {
        return atomic;
    }

    @Required
    public void setAtomicArchiveOfMultipleObjects(boolean atomic) {
        this.atomic = atomic;
    }
    
    public Collection<Object[]> getArchivedObjectsOperations() {
        return Collections.unmodifiableCollection(new ArrayList<Object[]>(archivedObjectsOperations));
    }
}