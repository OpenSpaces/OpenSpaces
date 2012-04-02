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
package org.openspaces.admin.alert.alerts;

import org.openspaces.admin.alert.Alert;

/**
 * Strongly typed wrapper for an Alert
 * @author itaif
 * @since 9.0.0
 */
public class ElasticAutoScalingAlert extends AbstractAlert {
    
    private static final long serialVersionUID = 1L;
    
    /** required by java.io.Externalizable */
    public ElasticAutoScalingAlert() {
    }
    
    public ElasticAutoScalingAlert(Alert alert) {
        super(alert);
    }
}
