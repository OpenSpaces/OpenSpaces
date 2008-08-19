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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class LoadBalancerInfo {

    private String name;

    private Map<Integer, LoadBalancerNodeInfo> balancers = new ConcurrentHashMap<Integer, LoadBalancerNodeInfo>();

    private volatile boolean dirty = true;

    public LoadBalancerInfo(String name) {
        this.name = name;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getName() {
        return name;
    }

    public void putNode(LoadBalancerNodeInfo balancerInfo) {
        balancers.put(balancerInfo.getClusterInfo().getRunningNumberOffset1(), balancerInfo);
    }

    public void removeNode(LoadBalancerNodeInfo balancerInfo) {
        balancers.remove(balancerInfo.getClusterInfo().getRunningNumberOffset1());
    }

    public LoadBalancerNodeInfo[] getNodes() {
        return balancers.values().toArray(new LoadBalancerNodeInfo[0]);
    }
}
