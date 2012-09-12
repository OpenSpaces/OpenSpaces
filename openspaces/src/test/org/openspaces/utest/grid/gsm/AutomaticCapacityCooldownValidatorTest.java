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
package org.openspaces.utest.grid.gsm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.grid.gsm.autoscaling.AutomaticCapacityCooldownValidator;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingTemporarilyDisabledCooldownException;

/**
 * @author itaif
 * @since 9.0.0
 */
public class AutomaticCapacityCooldownValidatorTest extends TestCase {

    AutomaticCapacityCooldownValidator validator;
    
    @Override
    protected void setUp() {
        validator = new AutomaticCapacityCooldownValidator();
        validator.setCooldownAfterInstanceAdded(2, TimeUnit.MILLISECONDS);
        validator.setCooldownAfterInstanceRemoved(5, TimeUnit.MILLISECONDS);    
    }
    
    @Test
    public void testInstanceAddedCooldown() {
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance2"), 0L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance2"), 1L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance2"), 3L);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            Assert.assertEquals("Unexpected exception", null, e);
        }
    }
    
    @Test
    public void testInstanceRemovedCooldown() {
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance2"), 0L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1"), 10L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1"), 11L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1"), 16L);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            Assert.assertEquals("Unexpected exception", null, e);
        }
    }
    
    @Test
    public void testInstanceStartedCooldown() {
        
        try {
            validator.validate(DeploymentStatus.BROKEN, instancesUids("instance1"), 0L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1"), 10L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1"), 11L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1"), 13L);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            Assert.assertEquals("Unexpected exception", null, e);
        }
    }
    
    @Test
    public void testInstanceRemovedAndAddedCooldown() {
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance2"), 0L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance3"), 10L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance3"), 11L);
            Assert.assertTrue("Expected exception",false);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            //expected
        }
        
        try {
            validator.validate(DeploymentStatus.INTACT, instancesUids("instance1","instance3"), 16L);
        } catch (AutoScalingTemporarilyDisabledCooldownException e) {
            Assert.assertEquals("Unexpected exception", null, e);
        }
    }
    
    private Set<String> instancesUids(String... uids) {
        Set<String> instancesUids = new HashSet<String>();
        for (String uid : uids) {
            instancesUids.add(uid);
        }
        return instancesUids;
    }
}
