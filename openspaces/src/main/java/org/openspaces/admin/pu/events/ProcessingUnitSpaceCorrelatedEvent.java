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

package org.openspaces.admin.pu.events;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;

/**
 * An event that indicates that an (embedded) {@link Space} was correlated with the processing unit.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnit#getSpaceCorrelated()
 */
public class ProcessingUnitSpaceCorrelatedEvent {

    private final Space space;

    private final ProcessingUnit processingUnit;

    public ProcessingUnitSpaceCorrelatedEvent(Space space, ProcessingUnit processingUnit) {
        this.space = space;
        this.processingUnit = processingUnit;
    }

    /**
     * Returns the space that was correlated with the processing unit.
     */
    public Space getSpace() {
        return space;
    }

    /**
     * Returns the Processing Unit the space was correlated with.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }
}
