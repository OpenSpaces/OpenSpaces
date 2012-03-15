/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.pu;

import org.openspaces.admin.AdminException;

/**
 * @author kimchy (shay.banon)
 */
public class ProcessingUnitAlreadyDeployedException extends AdminException {

    private static final long serialVersionUID = 7237728063214305847L;
    private final String processingUnitName;

    public ProcessingUnitAlreadyDeployedException(String name) {
        super("processing unit [" + name + "] already deployed");
        this.processingUnitName = name;
    }
    
    public String getProcessingUnitName() {
        return this.processingUnitName;
    }
}
