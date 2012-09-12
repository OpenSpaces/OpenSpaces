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

import com.gigaspaces.lrmi.nio.info.NIODetails;
import org.openspaces.admin.transport.TransportDetails;

/**
 * @author kimchy
 */
public class DefaultTransportDetails implements TransportDetails {

    private final NIODetails nioDetails;

    public DefaultTransportDetails(NIODetails nioDetails) {
        this.nioDetails = nioDetails;
    }

    public String getBindHost() {
        return nioDetails.getBindHost();
    }

    public String getHostAddress() {
        if (nioDetails.getHostAddress().length() == 0) {
            return nioDetails.getBindHost();
        }
        return nioDetails.getHostAddress();
    }

    public String getHostName() {
        if (nioDetails.getHostName().length() == 0) {
            return nioDetails.getBindHost();
        }
        return nioDetails.getHostName();
    }

    public int getPort() {
        return nioDetails.getPort();
    }

    public int getMinThreads() {
        return nioDetails.getMinThreads();
    }

    public int getMaxThreads() {
        return nioDetails.getMaxThreads();
    }

    public boolean isSslEnabled() {
        return nioDetails.isSslEnabled();
    }
}
