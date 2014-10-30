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
package org.openspaces.admin.internal.pu.elastic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openspaces.core.util.StringProperties;


public class GridServiceAgentFailureDetectionConfig {

	public enum FailureDetectionStatus {
		DISABLE_FAILURE_DETECTION,
		ENABLE_FAILURE_DETECTION,
		DONT_CARE
	}
	
	// each key is an ip address of a machine that its 
    private static final String DISABLED_FAILURE_DETECTION_KEY = "agent.disabled-failure-detection.";

    StringProperties properties;

    public GridServiceAgentFailureDetectionConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    private Map<String, String> getDisabledFailureDetection() {
        return properties.getMap(DISABLED_FAILURE_DETECTION_KEY, null);
    }

    /**
     * A set of machine ip addresses for which failure detection is temporarily disabled, 
     * and the expire timestamp after which it should be enabled again. 
     */
    private void setDisabledFailureDetection(Map<String, String> disabledFailureDetection) {
        properties.putMap(DISABLED_FAILURE_DETECTION_KEY, disabledFailureDetection);
    }

    /**
     * Temporarily disable failure detection of agents in the specified ip address until the specified timestamp.
     */
    public void disableFailureDetection(String ipAddress, long expireTimestamp) {
        Map<String, String> ipAddresses = this.getDisabledFailureDetection();
        if (ipAddresses == null)
            ipAddresses = new HashMap<String, String>();
        ipAddresses.put(ipAddress, String.valueOf(expireTimestamp));
        this.setDisabledFailureDetection(ipAddresses);
    }

    /**
     * Enable failure detection of agents in the specified IP address.
     */
    public void enableFailureDetection(String ipAddress) {
        disableFailureDetection(ipAddress, 0);
    }
     
    public FailureDetectionStatus getFailureDetectionStatus(String ipAddress, long now) {
        final Map<String, String> disabledFailureDetection = getDisabledFailureDetection();
		final String value = disabledFailureDetection != null ? disabledFailureDetection.get(ipAddress) : null;
    	if (value == null) {
			return FailureDetectionStatus.DONT_CARE;
		}
    	final Long expireTimestamp = Long.valueOf(value);
		if (now < expireTimestamp) {
			return FailureDetectionStatus.DISABLE_FAILURE_DETECTION;
		}
		return FailureDetectionStatus.ENABLE_FAILURE_DETECTION;
	}
    
    public Set<String> getFailureDetectionIpAddresses() {
        Map<String, String> disabledFailureDetection = getDisabledFailureDetection();
    	return disabledFailureDetection != null ? disabledFailureDetection.keySet() : Collections.EMPTY_SET;
    }
    
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GridServiceContainerConfig other = (GridServiceContainerConfig) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }
}
