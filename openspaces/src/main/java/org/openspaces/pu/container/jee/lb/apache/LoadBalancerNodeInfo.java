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

package org.openspaces.pu.container.jee.lb.apache;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import net.jini.core.lookup.ServiceID;

/**
 * Node level information for the load balancer.
 *
 * @author kimchy
 */
public class LoadBalancerNodeInfo {

    private ServiceID serviceID;

    private ClusterInfo clusterInfo;

    private JeeServiceDetails serviceDetails;

    public LoadBalancerNodeInfo(ServiceID serviceID, ClusterInfo clusterInfo, JeeServiceDetails serviceDetails) {
        this.serviceID = serviceID;
        this.clusterInfo = clusterInfo;
        this.serviceDetails = serviceDetails;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    /**
     * Returns the cluster specific information fo the load balancer node (the web application processing
     * unit instance).
     */
    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    /**
     * Returns jee related information for the given load balancer node (for example, host, port, and context path).
     */
    public JeeServiceDetails getServiceDetails() {
        return serviceDetails;
    }
}
