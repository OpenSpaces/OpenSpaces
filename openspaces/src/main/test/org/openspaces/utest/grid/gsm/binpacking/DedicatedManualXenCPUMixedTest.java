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
package org.openspaces.utest.grid.gsm.binpacking;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.machines.BinPackingSolver;

/**
 * @see test.gsm.datagrid.manual.cpu.xen.DedicatedManualXenCPUMixedTest
 * @author Moran Avigdor
 */
public class DedicatedManualXenCPUMixedTest extends TestCase {

    private static final Log logger = LogFactory.getLog(DedicatedManualXenCPUMixedTest.class);

    private static final int _256_MB_ = 256;
    private static final int _512_MB_ = 512;
    private static final long _6144_MB_ = 6144;
    private static final long _1024_MB_ = 1024;

    private static final long MACHINE_MEMORY_IN_MEGABYTES = 1500;
    private static final long RESERVED_MEMORY_PER_MACHINE_MEGABYTES = 128;
    private static final long _1372_MB_ = MACHINE_MEMORY_IN_MEGABYTES - RESERVED_MEMORY_PER_MACHINE_MEGABYTES;
    
    @Test
    public void test() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setContainerMemoryCapacityInMB(_256_MB_);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(_6144_MB_);
        solver.setMinimumNumberOfMachines(2);
        
        solver.setAllocatedCapacityForPu(new ClusterCapacityRequirements());
        
        solver.setUnallocatedCapacity(new ClusterCapacityRequirements()
            .add("AGENT_A",cpuMemory(new Fraction(2), _1372_MB_))
            .add("AGENT_B",cpuMemory(new Fraction(2), _1372_MB_))
            .add("AGENT_C",cpuMemory(new Fraction(2), _1372_MB_))
            .add("AGENT_D",cpuMemory(new Fraction(2), _1372_MB_))
        );
        
        //scale-out to 2 machines (2 containers each)
        solver.solveManualCapacityScaleOut(cpuMemory(new Fraction(4), _1024_MB_));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(4), getCpuCores(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(_1024_MB_, getMemoryInMB(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(Fraction.ZERO, getCpuCores(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(0, getMemoryInMB(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        assertMaxContainersPerAgent(solver, 2);

        solver.reset();
        
        //scale-out to 4 machines (1 containers each)
        solver.solveManualCapacityScaleOut(cpuMemory(new Fraction(4), 0));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(4), getCpuCores(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(_512_MB_, getMemoryInMB(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(Fraction.ZERO, getCpuCores(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(_512_MB_, getMemoryInMB(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        assertMaxContainersPerAgent(solver, 1);
        
        solver.reset();
        
        //now shrink back to 2 machines
        //scale-in to 2 machines (2 containers each)
        solver.solveManualCapacityScaleIn(cpuMemory(new Fraction(4), 0));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, getCpuCores(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(_512_MB_, getMemoryInMB(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(new Fraction(4), getCpuCores(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(_512_MB_, getMemoryInMB(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        assertMaxContainersPerAgent(solver, 2);

        solver.reset();
        
        //scale-out (by memory) to 5 machines (2 to 3 containers each, total 12 containers)
        solver.setUnallocatedCapacity(
                solver.getUnallocatedCapacity()
                    .add("AGENT_E",cpuMemory(new Fraction(2), _1372_MB_)
        ));
        solver.solveManualCapacityScaleOut(cpuMemory(Fraction.ZERO, 6000 - _1024_MB_));
        Assert.assertEquals(5, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(_6144_MB_-_1024_MB_, getMemoryInMB(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity()));
        Assert.assertEquals(0, getMemoryInMB(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity()));
        double numberOfMachines = 5.0;
        assertMaxContainersPerAgent(solver, (int)Math.ceil(_6144_MB_/_256_MB_/numberOfMachines));
        
        Fraction allocatedCpuCores = getCpuCores(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Fraction deallocatedCpuCores = getCpuCores(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertTrue("Need to retain number of cpu cores",allocatedCpuCores.compareTo(deallocatedCpuCores)>=0);

    }

    private CapacityRequirements cpuMemory(Fraction cpu, long memory) {
        return new CapacityRequirements(new CpuCapacityRequirement(cpu),new MemoryCapacityRequirement(memory));
    }

    private void assertMaxContainersPerAgent(BinPackingSolver solver, int maxContainers) {
        for (String agentUid : solver.getAllocatedCapacityForPu().getAgentUids()) {
            int containersPerAgent = getContainersPerAgent(solver, agentUid);
            assertTrue("containers per agent is " + containersPerAgent + " "+
                    "which is more than " + maxContainers,containersPerAgent <= maxContainers);
        }
    }

    private int getContainersPerAgent(BinPackingSolver solver, String agentUid) {
        final long allocatedMemory = getMemoryInMB(solver.getAllocatedCapacityForPu().getAgentCapacity(agentUid));
        final int numberOfContainers = (int) (allocatedMemory / _256_MB_);
        return numberOfContainers;
    }

    private Fraction getCpuCores(CapacityRequirements totalAllocatedCapacity) {
        return totalAllocatedCapacity.getRequirement(new CpuCapacityRequirement().getType()).getCpu();
    }

    private int getMemoryInMB(CapacityRequirements totalAllocatedCapacity) {
        return (int) totalAllocatedCapacity.getRequirement(new MemoryCapacityRequirement().getType()).getMemoryInMB();
    }

}
