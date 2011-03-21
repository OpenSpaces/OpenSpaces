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


public class SharedStatelessManualXenFourMixedDeploymentsTest extends TestCase {
    
    private static final Log logger = LogFactory.getLog(SharedStatelessManualXenFourMixedDeploymentsTest.class);

    private static final long _165_MB_ = 165;
    private static final long _330_MB_ = 330;
    private static final long _495_MB_ = 495;
    private static final long _660_MB_ = 660;
    
    private static final long MACHINE_MEMORY_IN_MEGABYTES = 1500;
    private static final long RESERVED_MEMORY_PER_MACHINE_MEGABYTES = 128;
    private static final long _1372_MB_ = MACHINE_MEMORY_IN_MEGABYTES - RESERVED_MEMORY_PER_MACHINE_MEGABYTES;
    
    @Test
    public void test() {
        
        BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setContainerMemoryCapacityInMB(_165_MB_);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(_495_MB_);
        solver.setMinimumNumberOfMachines(1);
        
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        
        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity()
            .add("AGENT_A",new AllocatedCapacity(new Fraction(2), _330_MB_))
        );
        
        // initial "scale out" to 1 machine
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(new Fraction(2), _330_MB_));
        Assert.assertEquals(1, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(2), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(_330_MB_, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        assertContainersPerAgent(solver,2, 2);
        
        // scale out to 2 machines
        solver.setUnallocatedCapacity(
                solver.getUnallocatedCapacity()
                    .add("AGENT_B", new AllocatedCapacity(new Fraction(2), _1372_MB_ - _660_MB_)
        ));
        
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(new Fraction(0), _165_MB_));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(4), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(_495_MB_, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        assertContainersPerAgent(solver,1,2);
        

    }

    private void assertContainersPerAgent(BinPackingSolver solver, int minContainers, int maxContainers) {
        for (String agentUid : solver.getAllocatedCapacityForPu().getAgentUids()) {
            int containersPerAgent = getContainersPerAgent(solver, agentUid);
            assertTrue("expected: containersPerAgent >= minContainers, actual: "+containersPerAgent+">=" + minContainers, containersPerAgent >= minContainers);
            assertTrue("expected: containersPerAgent <= maxContainers, actual: "+containersPerAgent+"<=" + maxContainers, containersPerAgent <= maxContainers);
        }
    }

    private int getContainersPerAgent(BinPackingSolver solver, String agentUid) {
        final long allocatedMemory = solver.getAllocatedCapacityForPu().getAgentCapacity(agentUid).getMemoryInMB();
        final int numberOfContainers = (int) (allocatedMemory / _165_MB_);
        return numberOfContainers;
    }
    

}
