package org.openspaces.utest.grid.gsm.binpacking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.machines.BinPackingSolver;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @see test.gsm.datagrid.manual.cpu.xen.DedicatedManualXenCPUMixedTest
 * @author Moran Avigdor
 */
public class DedicatedManualXenCPUMixedTest extends TestCase {

    private static final Log logger = LogFactory.getLog(DedicatedManualXenCPUMixedTest.class);

    private static final int _256_MB_ = 256;
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
        
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        
        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity()
            .add("AGENT_A",new AllocatedCapacity(new Fraction(2), _1372_MB_))
            .add("AGENT_B",new AllocatedCapacity(new Fraction(2), _1372_MB_))
            .add("AGENT_C",new AllocatedCapacity(new Fraction(2), _1372_MB_))
            .add("AGENT_D",new AllocatedCapacity(new Fraction(2), _1372_MB_))
        );
        
        //scale-out to 2 machines (2 containers each)
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(new Fraction(4), _1024_MB_));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(4), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(_1024_MB_, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        assertMaxContainersPerAgent(solver, 2);
        
        //scale-out to 4 machines (4 containers each)
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(new Fraction(8), 0));
        Assert.assertEquals(4, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(8), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(_1024_MB_, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        assertMaxContainersPerAgent(solver, 4);
        
        //now shrink back to 2 machines
        //scale-in to 2 machines (2 containers each)
        solver.solveManualCapacityScaleIn(new AllocatedCapacity(new Fraction(4), 0));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(4), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(_1024_MB_, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        assertMaxContainersPerAgent(solver, 2);
        
        //scale-out (by memory) to 5 machines (23-24 containers each)
        solver.setUnallocatedCapacity(
                solver.getUnallocatedCapacity()
                    .add("AGENT_E",new AllocatedCapacity(new Fraction(2), _1372_MB_)
        ));
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(Fraction.ZERO, _6144_MB_));
        Assert.assertEquals(5, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(8), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(_6144_MB_, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        assertMaxContainersPerAgent(solver, 24);
    }

    private void assertMaxContainersPerAgent(BinPackingSolver solver, int maxContainers) {
        for (String agentUid : solver.getAllocatedCapacityForPu().getAgentUids()) {
            assertTrue(getContainersPerAgent(solver, agentUid) <= maxContainers);
        }
    }

    private int getContainersPerAgent(BinPackingSolver solver, String agentUid) {
        final long allocatedMemory = solver.getAllocatedCapacityForPu().getAgentCapacity(agentUid).getMemoryInMB();
        final int numberOfContainers = (int) (allocatedMemory / _256_MB_);
        return numberOfContainers;
    }
    
}
