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

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;

/**
 * An event indicating that a processing unit instance level statistics has changed.
 *
 * <p>Note, monitoring needs to be enabled in order to receive the events.
 *
 * @author kimchy
 */
public class ProcessingUnitInstanceStatisticsChangedEvent {

    private final ProcessingUnitInstance processingUnitInstance;

    private final ProcessingUnitInstanceStatistics statistics;

    public ProcessingUnitInstanceStatisticsChangedEvent(ProcessingUnitInstance processingUnitInstance, ProcessingUnitInstanceStatistics statistics) {
        this.processingUnitInstance = processingUnitInstance;
        this.statistics = statistics;
    }

    public ProcessingUnitInstance getProcessingUnitInstance() {
        return processingUnitInstance;
    }

    public ProcessingUnitInstanceStatistics getStatistics() {
        return statistics;
    }
}