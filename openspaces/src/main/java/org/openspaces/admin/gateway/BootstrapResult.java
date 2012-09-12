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
package org.openspaces.admin.gateway;

/**
 * A result of a bootstrap
 * 
 * @see GatewaySinkSource#bootstrapFromGatewayAndWait(long, java.util.concurrent.TimeUnit)
 * @author eitany
 * @since 8.0.4
 */
public interface BootstrapResult {
   
    /**
     * Returns <code>true</code> if the bootstrap succeeded, <code>false</code> otherwise. 
     */
    boolean isSucceeded();
    
    /**
     * Returns the failure cause for the bootstrap or <code>null</code> if the bootstrap succeeded. 
     */
    Throwable getFailureCause();

}
