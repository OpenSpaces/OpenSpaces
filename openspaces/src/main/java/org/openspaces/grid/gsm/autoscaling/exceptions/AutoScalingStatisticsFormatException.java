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
package org.openspaces.grid.gsm.autoscaling.exceptions;

import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

/**
 * @author itaif
 * @since 9.0.0
 */
public class AutoScalingStatisticsFormatException extends AutoScalingStatisticsException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;

    public AutoScalingStatisticsFormatException(ProcessingUnit pu, Object value, Object threshold, NumberFormatException e) {
        super(pu,message(value,threshold),e);
    }
    
    private static String message(Object value, Object threshold) {
        return "Failed to compare value (" + value + ") with high threshold (" + threshold +")";
    }
    
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticAutoScalingFailureEvent event = new DefaultElasticAutoScalingFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitNames(getAffectedProcessingUnits());
        return event;
    }
}
