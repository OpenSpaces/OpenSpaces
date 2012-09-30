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

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * @author itaif
 * @since 9.0.1
 */
public class AutoScalingLowThresholdBreachedException extends AutoScalingThresholdBreachedException {

    private static final long serialVersionUID = 1L;

    public AutoScalingLowThresholdBreachedException(
            ProcessingUnit pu,
            CapacityRequirements actual,
            CapacityRequirements newPlan,
            long containerCapacityInMB) {
        super(message(pu, actual, newPlan), pu, actual, newPlan, containerCapacityInMB);
    }

    private static String message(ProcessingUnit pu, CapacityRequirements actual, CapacityRequirements newPlan) {
        //TODO: Add rule, threshold, value that breached threshold, how value was calculated.
        return "Decreasing planned capacity of " + pu.getName() + " to " + newPlan +". Actual capacity is " + actual;
    }
}
