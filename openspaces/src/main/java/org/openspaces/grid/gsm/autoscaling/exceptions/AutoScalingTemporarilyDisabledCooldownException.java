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

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.pu.ProcessingUnit;

/**
 * Indicates that auto scaling rules are disabled due to cooldown period after instance
 * added or instance removed.
 * 
 * @author itaif
 * @since 9.0.0
 */
public class AutoScalingTemporarilyDisabledCooldownException extends AutoScalingSlaEnforcementInProgressException {
    
    private static final long serialVersionUID = 1L;

    public AutoScalingTemporarilyDisabledCooldownException(ProcessingUnit pu, long timeLeftMillis) {
        super(pu, message(timeLeftMillis));
    }

    private static String message(long timeLeftMillis) {
        return "Auto Scaling is temporarily disabled for the next " + 
                TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis);
    }

}
