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
package org.openspaces.admin.internal.pu.statistics;

import java.util.Set;

import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

/**
 * @author itaif
 * 
 */
public interface InternalProcessingUnitStatisticsCalculator {

    /**
     * Generates more statistics using time aggregation techniques
     * 
     * @param processingUnitStatistics
     *            - The object which existing statistics are read, and generated statistics are written to. 
     *            Reading existing statistics using
     *            {@link ProcessingUnitStatistics#getStatistics()}
     *            Previous statistics are accessed using
     *            {@link ProcessingUnitStatistics#getPrevious()}
     *            New statistics are added using
     *            {@link InternalProcessingUnitStatistics#addStatistics(ProcessingUnitStatisticsId, Object)}
     *            
     * @param statisticsIds - statistics Ids that require calculation
     */
    void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics, Set<ProcessingUnitStatisticsId> statisticsIds);

}
