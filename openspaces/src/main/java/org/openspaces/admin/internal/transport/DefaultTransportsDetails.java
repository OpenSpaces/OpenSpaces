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
package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportsDetails;

/**
 * @author kimchy
 */
public class DefaultTransportsDetails implements TransportsDetails {

    private final TransportDetails[] details;

    public DefaultTransportsDetails(TransportDetails[] details) {
        this.details = details;
    }

    public int getMinThreads() {
        int total = 0;
        for (TransportDetails detail : details) {
            total += detail.getMinThreads();
        }
        return total;
    }

    public int getMaxThreads() {
        int total = 0;
        for (TransportDetails detail : details) {
            total += detail.getMaxThreads();
        }
        return total;
    }
}
