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
package org.openspaces.utest.admin.pu.elastic.config;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openspaces.admin.internal.pu.elastic.GridServiceAgentFailureDetectionConfig;
import org.openspaces.admin.internal.pu.elastic.GridServiceAgentFailureDetectionConfig.FailureDetectionStatus;

import java.util.HashMap;

/**
 * Tests {@link GridServiceAgentFailureDetectionConfig}
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class GridServiceAgentFailureDetectionConfigTest {
    
    private static final String IP = "127.0.0.1";

    @Test
    @Ignore("Requires further investigation")
    public void testDontCare() {
		GridServiceAgentFailureDetectionConfig c = new GridServiceAgentFailureDetectionConfig(new HashMap<String,String>());
    	Assert.assertEquals(FailureDetectionStatus.DONT_CARE, c.getFailureDetectionStatus(IP, 10000));
    }
    
	@Test
    public void testDisabled() {
    	GridServiceAgentFailureDetectionConfig c = new GridServiceAgentFailureDetectionConfig(new HashMap<String,String>());
    	c.disableFailureDetection(IP, 100000);
        Assert.assertEquals(FailureDetectionStatus.DISABLE_FAILURE_DETECTION, c.getFailureDetectionStatus(IP, 0));
    }
	
	@Test
    public void testDisabledExpired() {
		GridServiceAgentFailureDetectionConfig c = new GridServiceAgentFailureDetectionConfig(new HashMap<String,String>());
    	c.disableFailureDetection(IP, 0);
        Assert.assertEquals(FailureDetectionStatus.ENABLE_FAILURE_DETECTION, c.getFailureDetectionStatus(IP, 10000));
    }
        
}
