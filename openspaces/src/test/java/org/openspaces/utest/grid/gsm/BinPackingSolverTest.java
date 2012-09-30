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
package org.openspaces.utest.grid.gsm;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.machines.BinPackingSolver;

public class BinPackingSolverTest extends TestCase {

    private static final Log logger = LogFactory.getLog(BinPackingSolverTest.class);

    // agent1 can hold 3 containers
    private static final String AGENT1_UID = "AGENT1";
    private static final String AGENT2_UID = "AGENT2";
    private static final String AGENT3_UID = "AGENT3";
    private static final String AGENT4_UID = "AGENT4";
    
    private static final long MAX_MEMORY_MEGABYTES = 512 * 100;

    private static final int CONTAINER_MEMORY = 512;
    private static final CapacityRequirements CONTAINER_CAPACITY = cpuMemoryDrive(Fraction.TWO, CONTAINER_MEMORY, 1024);
    
    
    /**
     * Tests allocation of a single container on a single machine using number of machines solver.
     */
    @Test
    public void testOneMachine() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(1);
        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(3));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.solveNumberOfMachines(1);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());

        // when asking for 1 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a two machines given one unallocated machine.
     */
    @Test
    public void testTwoMachinesOverflow() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(2);
        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,CONTAINER_CAPACITY.multiply(3));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.solveNumberOfMachines(2);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());

        // when asking for a machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 512, 0), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a two machines given three unallocated machines.
     */
    @Test
    public void testTwoMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(3))
            .add(AGENT2_UID, CONTAINER_CAPACITY.multiply(3))
            .add(AGENT3_UID, CONTAINER_CAPACITY.multiply(3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(2);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());

        // when asking for 2 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 512, 0), allocatedCapacity.getAgentCapacity(agentUid));
        }
    }
    /**
     * Tests allocation of a two machines given three unallocated machines, two of them already
     * occupied by this pu.
     */
    @Test
    public void testTwoNewMachinesOverflow() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY)
            .add(AGENT2_UID, CONTAINER_CAPACITY));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,CONTAINER_CAPACITY.multiply(3))
                .add(AGENT2_UID,CONTAINER_CAPACITY.multiply(3))
                .add(AGENT3_UID,CONTAINER_CAPACITY.multiply(3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(2);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertFalse(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertFalse(allocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT3_UID));

        // when asking for machines, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 512, 0), allocatedCapacity.getAgentCapacity(AGENT3_UID));
    }

    /**
     * Tests allocation of a two machines given three unallocated machines.
     */
    @Test
    public void testThreeMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,CONTAINER_CAPACITY.multiply(3))
                .add(AGENT2_UID,CONTAINER_CAPACITY.multiply(3))
                .add(AGENT3_UID,CONTAINER_CAPACITY.multiply(3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(3);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT3_UID));
        Assert.assertEquals(3, allocatedCapacity.getAgentUids().size());

        // when asking for machines, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), allocatedCapacity.getAgentCapacity(AGENT1_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), allocatedCapacity.getAgentCapacity(AGENT2_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), allocatedCapacity.getAgentCapacity(AGENT3_UID));
    }

    /**
     * Tests allocation of a single container on a single machine using capacity solver.
     */
    @Test
    public void testCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent().add(AGENT1_UID,
                CONTAINER_CAPACITY.multiply(3));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(CONTAINER_CAPACITY);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(CONTAINER_CAPACITY, allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }


    /**
     * Test allocation of 4 containers.
     */
    public void testCapacityFourContainers() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(2))
            .add(AGENT2_UID, CONTAINER_CAPACITY.multiply(2));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(CONTAINER_CAPACITY.multiply(4));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());

        // when asking for 2 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(CONTAINER_CAPACITY.multiply(2), allocatedCapacity.getAgentCapacity(agentUid));
        }
    }


    /**
     * Tests allocation of two containers capacity on a single machine that has only room for one
     * container.
     */
    @Test
    public void testCapacityOverflow() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent().add(AGENT1_UID,
                CONTAINER_CAPACITY);
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(CONTAINER_CAPACITY.multiply(2));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(CONTAINER_CAPACITY, allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a backup container on agent1 that already has 2 primary containers using
     * capacity solver.
     */
    @Test
    public void testIncrementalCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(2)));
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent().add(AGENT1_UID,
                CONTAINER_CAPACITY);
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a primary container on agent1 that only has 2 CPU cores left, but no
     * memory left.
     */
    @Test
    public void testOverflowIncrementalCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(2)));
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, cpuMemoryDrive(Fraction.TWO, 0, 1024));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(Fraction.ZERO, 512, 0));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertFalse(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
    }

    /**
     * Tests allocation of two more CPU cores on agent1 that already has a container deployed.
     */
    @Test
    public void testIncrementalCpuCoreCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(2)));
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,CONTAINER_CAPACITY);
        solver.setUnallocatedCapacity(unallocatedCapacity);
        // request only cpu cores, not memory
        CapacityRequirements capacityToAllocate = cpuMemoryDrive(Fraction.TWO, 0, 0);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(capacityToAllocate, allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of more disk on agent1 that already has a container deployed.
     */
    @Test
    public void testIncrementalDiskCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(2)));
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,CONTAINER_CAPACITY);
        solver.setUnallocatedCapacity(unallocatedCapacity);
        // request only cpu cores, not memory
        CapacityRequirements capacityToAllocate = cpuMemoryDrive(Fraction.ZERO, 0, 1024);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(capacityToAllocate, allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }
    
    /**
     * Tests deallocation of a container that is moved from agent2 to agent3 due to cpu constraints
     * 
     * before:
     * agent1 allocated:  512MB RAM, 2 CPUs unallocated: 1024MB RAM, 4 CPUs
     * agent2 allocated: 1024MB RAM, 4 CPUs unallocated:    0MB RAM, 0 CPUs
     * agent3 allocated:    0MB RAM, 0 CPUs unallocated: 1024MB RAM, 4 CPUs
     * 
     * after allocating 6 more CPUs:
     * agent1 allocated:  512MB RAM, 4 CPUs unallocated: 2 CPUs
     * agent2 allocated:  512MB RAM, 4 CPUs unallocated: 0 CPUs
     * agent3 allocated:  512MB RAM, 4 CPUs unallocated: 0 CPUs
     * 
     */
    @Test
    public void testIncrementalCpuCoreCapacityWithDeallocation() {
        BinPackingSolver solver = new BinPackingSolver();
        CapacityRequirements containerCapacity = cpuMemoryDrive(Fraction.TWO, CONTAINER_MEMORY, 0);
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, containerCapacity)
                .add(AGENT2_UID, containerCapacity.multiply(2)));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,containerCapacity.multiply(2))
            .add(AGENT3_UID,containerCapacity.multiply(2)));

        CapacityRequirements capacityToAllocate = cpuMemoryDrive(new Fraction(6), 0, 0);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        CapacityRequirementsPerAgent deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertEquals(1, deallocatedCapacity.getAgentUids().size());
        Assert.assertTrue(deallocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), deallocatedCapacity.getAgentCapacity(AGENT2_UID));

        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(Fraction.TWO, 0, 0),
                allocatedCapacity.getAgentCapacityOrZero(AGENT1_UID));
        Assert.assertEquals(cpuMemoryDrive(new Fraction(4), CONTAINER_MEMORY, 0),
                allocatedCapacity.getAgentCapacityOrZero(AGENT3_UID));
    }

    /**
     * Tests deallocation of a container that is moved from agent2 to agent3 due to disk constraints
     * 
     * before:
     * agent1 allocated:  512MB RAM, 1024MB disk unallocated: 1024MB RAM, 2048MB disk
     * agent2 allocated: 1024MB RAM, 2048MB disk unallocated:    0MB RAM, 0 CPUs
     * agent3 allocated:    0MB RAM,    0MB disk unallocated: 1024MB RAM, 2048MB disk
     * 
     * after allocating more 3072MB disk:
     * agent1 allocated:  512MB RAM, 2048MB disk unallocated: 1024MB disk
     * agent2 allocated:  512MB RAM, 2048MB disk unallocated:    0MB disk
     * agent3 allocated:  512MB RAM, 2048MB disk unallocated:    0MB disk
     * 
     */
    @Test
    public void testIncrementalDiskCapacityWithDeallocation() {
        BinPackingSolver solver = new BinPackingSolver();
        CapacityRequirements containerCapacity = cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 1024);
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, containerCapacity)
                .add(AGENT2_UID, containerCapacity.multiply(2)));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,containerCapacity.multiply(2))
            .add(AGENT3_UID,containerCapacity.multiply(2)));

        CapacityRequirements capacityToAllocate = cpuMemoryDrive(Fraction.ZERO, 0, 1024*3);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        CapacityRequirementsPerAgent deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertEquals(1, deallocatedCapacity.getAgentUids().size());
        Assert.assertTrue(deallocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), deallocatedCapacity.getAgentCapacity(AGENT2_UID));

        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 0, 1024), allocatedCapacity.getAgentCapacityOrZero(AGENT1_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 1024*2), allocatedCapacity.getAgentCapacityOrZero(AGENT3_UID));
        
    }

    /**
     * Tests deallocation of a container that is moved from agent2 to agent3 due to disk and CPU constraints
     * 
     * before:
     * agent1 allocated:  512MB RAM, 2 CPUs, 1024MB disk unallocated: 1024MB RAM, 4 CPUs, 2048MB disk
     * agent2 allocated: 1024MB RAM, 4 CPUs, 2048MB disk unallocated:    0MB RAM, 0 CPUs, 0 CPUs
     * agent3 allocated:    0MB RAM, 0 CPUs,    0MB disk unallocated: 1024MB RAM, 4 CPUs, 2048MB disk
     *      * 
     * after allocating more 6 CPUs and 3072MB disk:
     * agent1 allocated:  512MB RAM, 4 CPUs, 2048MB disk unallocated: 1024MB disk, 2 CPUs
     * agent2 allocated:  512MB RAM, 4 CPUs, 2048MB disk unallocated: 0
     * agent3 allocated:  512MB RAM, 4 CPUs, 2048MB disk unallocated: 0
     * 
     */
    @Test
    public void testIncrementalCpuAndDiskCapacityWithDeallocation() {
        BinPackingSolver solver = new BinPackingSolver();
        CapacityRequirements containerCapacity = cpuMemoryDrive(Fraction.TWO, CONTAINER_MEMORY, 1024);
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, containerCapacity)
                .add(AGENT2_UID, containerCapacity.multiply(2)));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,containerCapacity.multiply(2))
            .add(AGENT3_UID,containerCapacity.multiply(2)));

        CapacityRequirements capacityToAllocate = cpuMemoryDrive(new Fraction(6,1), 0, 1024*3);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        CapacityRequirementsPerAgent deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertEquals(1, deallocatedCapacity.getAgentUids().size());
        Assert.assertTrue(deallocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), deallocatedCapacity.getAgentCapacity(AGENT2_UID));

        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(Fraction.TWO, 0, 1024), allocatedCapacity.getAgentCapacityOrZero(AGENT1_UID));
        Assert.assertEquals(cpuMemoryDrive(new Fraction(4), CONTAINER_MEMORY, 1024*2), allocatedCapacity.getAgentCapacityOrZero(AGENT3_UID));
        
    }

    /**
     * Tests allocation of too many containers, and rejects due to max memory
     */
    @Test
    public void testMaxNumberOfContainers() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY));

        CapacityRequirements capacityToAllocate = CONTAINER_CAPACITY.multiply(2);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        CapacityRequirementsPerAgent deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertTrue(deallocatedCapacity.equalsZero());

        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2), 0, 1024), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of too many containers, and rejects due to max memory
     */
    @Test
    public void testTwoMaxNumberOfContainers() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent().add(AGENT1_UID, CONTAINER_CAPACITY));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(CONTAINER_MEMORY *2);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,CONTAINER_CAPACITY.multiply(2))
            .add(AGENT2_UID,CONTAINER_CAPACITY.multiply(2)));

        CapacityRequirements capacityToAllocate = CONTAINER_CAPACITY.multiply(3);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(capacityToAllocate);
        CapacityRequirementsPerAgent deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertTrue(deallocatedCapacity.equalsZero());

        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(cpuMemoryDrive(Fraction.TWO, 0, 1024), allocatedCapacity.getAgentCapacity(AGENT1_UID));
        Assert.assertEquals(cpuMemoryDrive(new Fraction(4), 512, 2048), allocatedCapacity.getAgentCapacity(AGENT2_UID));
    }

    /**
     * Tests allocation of a 6 containers, given three machines each can hold three containers.
     */
    @Test
    public void testCapacitySixContainersThreeMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(3))
            .add(AGENT2_UID, CONTAINER_CAPACITY.multiply(3))
            .add(AGENT3_UID, CONTAINER_CAPACITY.multiply(3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(CONTAINER_CAPACITY.multiply(6));
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(CONTAINER_CAPACITY.multiply(3),
                    allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * Tests allocation of a 6 primary containers, given three machines each can hold two primary
     * containers and one backup container.
     */
    @Test
    public void testCapacitySixContainersThreeMachinesThatCanHoldTwoEach() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirements twoPrimariesOneBackupCapacity = 
            CONTAINER_CAPACITY.multiply(2)
            .add(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0));
        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent().add(AGENT1_UID,
                twoPrimariesOneBackupCapacity).add(AGENT2_UID, twoPrimariesOneBackupCapacity).add(AGENT3_UID,
                twoPrimariesOneBackupCapacity);
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(CONTAINER_CAPACITY.multiply(6));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        solver.getAllocatedCapacityResult();
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(3, allocatedCapacity.getAgentUids().size());
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(CONTAINER_CAPACITY.multiply(2),
                    allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * Tests allocation of a two "best" machines given three unallocated machines.
     */

    @Test
    public void testChooseTwoMachinesWithLessMemory() {

        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, CONTAINER_CAPACITY.multiply(6))
            .add(AGENT2_UID, CONTAINER_CAPACITY.multiply(2))
            .add(AGENT3_UID, CONTAINER_CAPACITY.multiply(3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(2);
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());

        // when asking for 2 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0), allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * starts with memory only (1000 containers on two machines) and then adds cpu requirements
     * (will cause a spread of 1000 containers to 1000 empty machines).
     */
    @Test
    public void testLargeScaleAllocation() {
        final int nMACHINES = 100;
        
        BinPackingSolver solver = new BinPackingSolver();
        //allocate 2 machines each with - 500 containers. Each container with 512 MB needing 1 CPU
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, cpuMemoryDrive(new Fraction(500), 512 * 500, 0))
                .add(AGENT2_UID, cpuMemoryDrive(new Fraction(500), 512 * 500, 0)));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(512 * nMACHINES * 100); // max = infinity
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(512 * nMACHINES * 100);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent();
        for (int i = 3; i <= nMACHINES; ++i) {
            // add more machines - each can occupy 1 container
            unallocatedCapacity = unallocatedCapacity.add("AGENT" + i, cpuMemoryDrive(Fraction.ONE, 512, 0));
        }

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        // goal - scale to nMACHINES machines.
        // result should be that the solver will add 998 containers in the 998 free machines
        // occupying 1 container per machine.
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(nMACHINES), 0, 0));
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, getCpuCores(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(0, getMemoryInMB(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        
        Assert.assertEquals(nMACHINES -2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(nMACHINES -2), getCpuCores(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(512 * (nMACHINES -2), getMemoryInMB(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
    }
    
    private Fraction getCpuCores(CapacityRequirements totalAllocatedCapacity) {
        return totalAllocatedCapacity.getRequirement(new CpuCapacityRequirement().getType()).getCpu();
    }

    private int getMemoryInMB(CapacityRequirements totalAllocatedCapacity) {
        return (int) totalAllocatedCapacity.getRequirement(new MemoryCapacityRequirement().getType()).getMemoryInMB();
    }

    /**
     * Check how much time does it takes when there is 1000 allocated machines without unallocated capacity, and a request for more memory.
     * This simulates the time period in which a new virtual machine is being started and we keep pounding the algorithm again and again. 
     */
    @Test
    public void testLargeScaleAllocation2() {
        final int nMACHINES = 100;
        
        BinPackingSolver solver = new BinPackingSolver();
        
        CapacityRequirementsPerAgent allocatedCapacity = new CapacityRequirementsPerAgent();
        for (int i=1; i<=nMACHINES; ++i) {
            allocatedCapacity = allocatedCapacity.add("AGENT" + i, cpuMemoryDrive(Fraction.ONE, 512, 0));
        }
        
        //allocate nMACHINES machines each with - 1 containers. Each container with 512 MB needing 1 CPU
        solver.setAllocatedCapacityForPu(allocatedCapacity);

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        
        //twice as much as we currently have allocated
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(2 * 512 * nMACHINES * 100);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(2 * 512 * nMACHINES * 100);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent();
        for (int i = 1; i <= nMACHINES; ++i) {
            //each can occupy less than 1 container
            unallocatedCapacity = unallocatedCapacity.add("AGENT" + i, cpuMemoryDrive(Fraction.ONE, 128, 0));
        }

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        // goal - scale by 512 MB.
        // result should be that the solver will not be able to move any containers
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(Fraction.ZERO, CONTAINER_MEMORY, 0));
        
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, getCpuCores(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(0, getMemoryInMB(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        
        Assert.assertEquals(0, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, getCpuCores(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(0, getMemoryInMB(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
    }
    
    
    @Test
    public void testRelocateCpuFromSourceMachine() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent()
                                .add(AGENT1_UID, cpuMemoryDrive(new Fraction(4), 128, 0))
                                .add(AGENT2_UID, cpuMemoryDrive(new Fraction(1), 128, 0)));
        
        solver.setContainerMemoryCapacityInMB(128);
        
        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent()
                                .add(AGENT2_UID, cpuMemoryDrive(new Fraction(5), 128*5, 0)));
        
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(1), 0, 0));
        
        Assert.assertEquals(new Fraction(6), getCpuCores(solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity()));
        Assert.assertEquals(256, getMemoryInMB(solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity()));
        
        Assert.assertEquals(1, solver.getDeallocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(1), 0, 0), solver.getDeallocatedCapacityResult().getAgentCapacity(AGENT1_UID));
        
        Assert.assertEquals(1, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2), 0, 0), solver.getAllocatedCapacityResult().getAgentCapacity(AGENT2_UID));
        
    }

    /**
     * Tests allocation of a fourth machine taking from one of the first three due to max memory limit
     */
    @Test
    public void testThreeMachinesScaleOutCpuToFourMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 500, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 500, 0))
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 500, 0))        
        );
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(6*250);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(250*1000);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = 
            new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 500, 0))
            .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 500, 0))
            .add(AGENT3_UID,cpuMemoryDrive(new Fraction(0), 500, 0))
            .add(AGENT4_UID,cpuMemoryDrive(new Fraction(2), 1000, 0));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(2),0,0));
        
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2),250, 0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0),250, 0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        
   }
    
    /**
     * Tests no allocation of a fourth machine due to max memory limit
     */
    @Test
    public void testThreeMachinesNoScaleOutCpuToFourMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 250, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 250, 0))        
        );
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(3*250);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(100*250);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = 
            new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 250, 0))
            .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 250, 0))
            .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 500, 0))
            .add(AGENT4_UID,cpuMemoryDrive(new Fraction(2), 500, 0));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(4), 0, 0));
        
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2),250, 0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0),0, 0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        
   }

    
    /**
     * Tests no allocation of a fourth machine due to max memory limit
     */
    @Test
    public void testTwoMachinesZeroBackupCpuCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(12*250);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(1000*250);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = 
            new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 12*250, 0));
            

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(4), 12*250, 0));
        
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2), 12*250, 0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0), 0, 0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        
   }
    

    /**
     * Tests no allocation of a fourth machine due to max memory limit
     */
    @Test
    public void testFourMachinesCpuScaleIn() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 250, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 250, 0))
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 500, 0))
                .add(AGENT4_UID,cpuMemoryDrive(new Fraction(2), 500, 0)));
        
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(8*250);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(100*250);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = 
            new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 250, 0))
            .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 250, 0));
            

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacityScaleIn(cpuMemoryDrive(new Fraction(0), 500, 0));
        
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0), 0, 0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0), 500, 0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
   }
    
    /**
     * Tests deallocation of one container accross all machines
     */
    @Test
    public void testScaleInAccrossAllMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, cpuMemoryDrive(new Fraction(4), 1024, 0))
                .add(AGENT2_UID, cpuMemoryDrive(new Fraction(8), 2048, 0))
                .add(AGENT3_UID, cpuMemoryDrive(new Fraction(4), 1024, 0)));

        solver.setContainerMemoryCapacityInMB(CONTAINER_MEMORY);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent());
        solver.setMinimumNumberOfMachines(1);
        
        CapacityRequirements capacityToDeallocate = cpuMemoryDrive(new Fraction(0), 512+1024+512, 0);
        solver.solveManualCapacityScaleIn(capacityToDeallocate);
        
        CapacityRequirementsPerAgent allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(0, allocatedCapacity.getAgentUids().size());
        
        CapacityRequirementsPerAgent deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertEquals(3, deallocatedCapacity.getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 512, 0), deallocatedCapacity.getAgentCapacity(AGENT1_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 1024, 0), deallocatedCapacity.getAgentCapacity(AGENT2_UID));
        Assert.assertEquals(cpuMemoryDrive(Fraction.ZERO, 512, 0), deallocatedCapacity.getAgentCapacity(AGENT3_UID));
    }
    
    @Test
    // 5 , 2 , 2 ==> 3, ,3 , 3
    public void testRebalancingFromOverMaxToUnderMin() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, cpuMemoryDrive(Fraction.ZERO, 500, 0))
                .add(AGENT2_UID, cpuMemoryDrive(Fraction.ZERO, 200, 0))
                .add(AGENT3_UID, cpuMemoryDrive(Fraction.ZERO, 200, 0)));

        solver.setContainerMemoryCapacityInMB(100);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setUnallocatedCapacity(
            new CapacityRequirementsPerAgent()
            .add(AGENT2_UID, cpuMemoryDrive(Fraction.ZERO, 100, 0))
            .add(AGENT3_UID, cpuMemoryDrive(Fraction.ZERO, 100, 0)));
        solver.setMinimumNumberOfMachines(1);
        
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(Fraction.ZERO, 0, 0));
        for (String agentUid : solver.getAllocatedCapacityForPu().getAgentUids()) {
            long memory = getMemoryInMB(solver.getAllocatedCapacityForPu().getAgentCapacity(agentUid));
            Assert.assertTrue(memory == 300);
        }
    }
    
    @Test
    // 5 , 3 , 3 ==> 4, 4 , 3
    public void testRebalancingFromOverMaxToMin() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, cpuMemoryDrive(Fraction.ZERO, 500, 0))
                .add(AGENT2_UID, cpuMemoryDrive(Fraction.ZERO, 300, 0))
                .add(AGENT3_UID, cpuMemoryDrive(Fraction.ZERO, 300, 0)));

        solver.setContainerMemoryCapacityInMB(100);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setUnallocatedCapacity(
            new CapacityRequirementsPerAgent()
            .add(AGENT1_UID, cpuMemoryDrive(Fraction.ZERO, 100, 0))
            .add(AGENT2_UID, cpuMemoryDrive(Fraction.ZERO, 100, 0))
            .add(AGENT3_UID, cpuMemoryDrive(Fraction.ZERO, 100, 0)));
        solver.setMinimumNumberOfMachines(1);
        
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(Fraction.ZERO, 0, 0));
        for (String agentUid : solver.getAllocatedCapacityForPu().getAgentUids()) {
            long memory = getMemoryInMB(solver.getAllocatedCapacityForPu().getAgentCapacity(agentUid));
            Assert.assertTrue("memory cannot be below 300", memory >= 300);
            Assert.assertTrue("memory cannot be above 400", memory <= 400);
        }
    }

    @Test
    // 4 , 4 , 2 ==> 4, 3 , 3
    public void testRebalancingToUnderMin() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID, cpuMemoryDrive(Fraction.ZERO, 400, 0))
                .add(AGENT2_UID, cpuMemoryDrive(Fraction.ZERO, 400, 0))
                .add(AGENT3_UID, cpuMemoryDrive(Fraction.ZERO, 200, 0)));

        solver.setContainerMemoryCapacityInMB(100);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setUnallocatedCapacity(
            new CapacityRequirementsPerAgent()
            .add(AGENT3_UID, cpuMemoryDrive(Fraction.ZERO, 100, 0)));
        solver.setMinimumNumberOfMachines(1);
        
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(Fraction.ZERO, 0, 0));
        for (String agentUid : solver.getAllocatedCapacityForPu().getAgentUids()) {
            long memory = getMemoryInMB(solver.getAllocatedCapacityForPu().getAgentCapacity(agentUid));
            Assert.assertTrue(agentUid + " memory is " + memory + " which is below " + 300,memory >= 300);
            Assert.assertTrue(agentUid + " memory is " + memory + " which is above " + 300,memory <= 400);
        }
    }

    @Test
    public void testScaleInWithManagementMachine() {
        
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 250, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 250, 0))
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 250, 0)));
        
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(8*250);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(1000 * 250);
        solver.setLogger(logger);

        CapacityRequirementsPerAgent unallocatedCapacity = 
            new CapacityRequirementsPerAgent()
            .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 250, 0))
            .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 250, 0))
            .add(AGENT3_UID,cpuMemoryDrive(new Fraction(0), 250, 0));
            
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        Map<String, Long> agentPriority = new HashMap<String, Long>();
        agentPriority.put(AGENT1_UID,100L);
        agentPriority.put(AGENT2_UID,20L);
        agentPriority.put(AGENT3_UID,3L);
        solver.setAgentAllocationPriority(agentPriority);
        
        solver.solveManualCapacityScaleIn(cpuMemoryDrive(new Fraction(2),250, 0));
        
        
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0),0, 0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2),250, 0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        String agentUidToRemove = solver.getDeallocatedCapacityResult().getAgentUids().iterator().next();
        Assert.assertEquals(AGENT3_UID,agentUidToRemove);
        
        solver.reset();
        solver.solveManualCapacityScaleIn(cpuMemoryDrive(new Fraction(2),250, 0));
        agentUidToRemove = solver.getDeallocatedCapacityResult().getAgentUids().iterator().next();
        Assert.assertEquals(AGENT2_UID,agentUidToRemove);
        
    }
    
    @Test
    public void testUndeploy() {

        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 250, 0)));
        solver.setUnallocatedCapacity(new CapacityRequirementsPerAgent());
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(8*250);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(100*250);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(0);
        
        solver.solveManualCapacityScaleIn(cpuMemoryDrive(new Fraction(2),250, 0));
        
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0), 0, 0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(2), 250, 0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        String agentUidToRemove = solver.getDeallocatedCapacityResult().getAgentUids().iterator().next();
        Assert.assertEquals(AGENT1_UID,agentUidToRemove);
    }
    
    @Test
    public void testScaleOutSmallResidualMemory() {
        
        BinPackingSolver solver = new BinPackingSolver();
        
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 750, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 750, 0)));
        
        solver.setUnallocatedCapacity(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 122, 0)) 
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 122, 0))
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 1372, 0)));
        
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(5000);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(100*250);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(2);
        
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(0), 500, 0));
        
        Assert.assertEquals(1,solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0), 500, 0),solver.getAllocatedCapacityResult().getAgentCapacity(AGENT3_UID));
        Assert.assertEquals(0,solver.getDeallocatedCapacityResult().getAgentUids().size());
    }

    @Test
    public void testScaleOutMaximumMemoryPerMachineConstraint() {
        
        BinPackingSolver solver = new BinPackingSolver();
        
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 500, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 500, 0)));
        
        solver.setUnallocatedCapacity(
                new CapacityRequirementsPerAgent()
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(0), 5000, 0)));
        
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(5000);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(100*250);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(2);
        
        solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(0), 5000, 0));
        
        Assert.assertEquals(1,solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(cpuMemoryDrive(new Fraction(0), 1000, 0),solver.getAllocatedCapacityResult().getAgentCapacity(AGENT3_UID));
        Assert.assertEquals(0,solver.getDeallocatedCapacityResult().getAgentUids().size());
    }
    
    @Test
    public void testScaleInWithAllocatedCapacityAboveHalfOfNewCapacityOnOneMachine() {
    BinPackingSolver solver = new BinPackingSolver();
        
        solver.setUnallocatedCapacity(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 220, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 412, 0))
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 412, 0)));
        
        solver.setAllocatedCapacityForPu(
                new CapacityRequirementsPerAgent()
                .add(AGENT1_UID,cpuMemoryDrive(new Fraction(0), 1152, 0))
                .add(AGENT2_UID,cpuMemoryDrive(new Fraction(0), 960, 0))
                .add(AGENT3_UID,cpuMemoryDrive(new Fraction(0), 960, 0)));
        
        solver.setContainerMemoryCapacityInMB(192);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(3072);
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(100*192);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(2);
        
        solver.solveManualCapacityScaleIn(cpuMemoryDrive(new Fraction(0), 1536, 0));
        Assert.assertEquals(1152+960+960-1536, getMemoryInMB(solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity()));
        Assert.assertEquals(2, solver.getAllocatedCapacityForPu().getAgentUids().size());
    }
    
    /**
     * GS-10036
     * Allocate new containers due to non-memory allocation requirements that requires the algorithm to spread out on all available machines.
     */
    @Test
    public void testSimulateScaleOutInEagerMode() {
        BinPackingSolver solver = new BinPackingSolver();
            
            solver.setUnallocatedCapacity(
                    new CapacityRequirementsPerAgent()
                    .add(AGENT1_UID,cpuMemoryDrive(new Fraction(2), 3699, 8989))
                    .add(AGENT2_UID,cpuMemoryDrive(new Fraction(2), 3699, 4928))
                    .add(AGENT3_UID,cpuMemoryDrive(new Fraction(2), 3699, 4928))
                    .add(AGENT4_UID,cpuMemoryDrive(new Fraction(2), 3699, 4928)));
            
            solver.setAllocatedCapacityForPu(
                    new CapacityRequirementsPerAgent());
            
            solver.setContainerMemoryCapacityInMB(256);
            solver.setMaxAllocatedMemoryCapacityOfPuInMB(256*4);
            solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(MAX_MEMORY_MEGABYTES);
            solver.setLogger(logger);
            solver.setMinimumNumberOfMachines(2);
            
            solver.solveManualCapacityScaleOut(cpuMemoryDrive(new Fraction(8), 23804, 1024));
            Assert.assertEquals(new Fraction(8), getCpuCores(solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity()));
            Assert.assertEquals(1024, getMemoryInMB(solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity()));
            Assert.assertEquals(4, solver.getAllocatedCapacityForPu().getAgentUids().size());
        }

    private static CapacityRequirements cpuMemoryDrive(Fraction cpu, int memoryInMB, int driveInMB) {
        return new CapacityRequirements(
                new CpuCapacityRequirement(cpu), 
                new MemoryCapacityRequirement((long)memoryInMB), 
                new DriveCapacityRequirement("d:", (long)driveInMB));
    }
}
