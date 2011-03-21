package org.openspaces.utest.grid.gsm.binpacking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.machines.BinPackingSolver;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @see test.gsm.datagrid.manual.memory.xen.DedicatedManualXenDataGridScaleOutAndScaleInTest
 * @author Moran Avigdor
 */
public class DedicatedManualXenDataGridScaleOutAndScaleInTest extends TestCase {

    private static final Log logger = LogFactory.getLog(DedicatedManualXenDataGridScaleOutAndScaleInTest.class);

    private static final long _3072_MB_ = MemoryUnit.GIGABYTES.toMegaBytes(3);
    private static final int _192_MB_ = 192;
    
    private static final long MACHINE_MEMORY_IN_MEGABYTES = 1500;
    private static final long RESERVED_MEMORY_PER_MACHINE_MEGABYTES = 128;
    private static final long _1372_MB_ = MACHINE_MEMORY_IN_MEGABYTES - RESERVED_MEMORY_PER_MACHINE_MEGABYTES;
    
    @Test
    public void test() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setContainerMemoryCapacityInMB(_192_MB_);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(_3072_MB_);
        solver.setMinimumNumberOfMachines(2);
        
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        
        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity()
            .add("AGENT_A",new AllocatedCapacity(new Fraction(2), _1372_MB_))
            .add("AGENT_B",new AllocatedCapacity(new Fraction(2), _1372_MB_))
            .add("AGENT_C",new AllocatedCapacity(new Fraction(2), _1372_MB_))
        );
        
        //scale-out to use 4 cores, 768 MB
        //result: 2 machines with total capacity of 4 cores and 768MB
        //{AGENT2=2 cores and 384MB, AGENT3=2 cores and 384MB}
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(Fraction.ZERO, _192_MB_* 4));
        Assert.assertEquals(2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO,_192_MB_*4),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getAgentUids().size());
        assertContainersPerAgent(solver, 0, 2);
        
        solver.reset();
        
        //scale-out to maximum capacity
        //result: 3 machines with total capacity of 6 cores and 3072MB
        //{AGENT3=2 cores and 1152MB, AGENT2=2 cores and 1152MB, AGENT1=2 cores and 768MB}
        //solver.solveManualCapacityScaleOut(new AllocatedCapacity(new Fraction(2), _3072_MB_));
        solver.solveManualCapacityScaleOut(new AllocatedCapacity(Fraction.ZERO, _192_MB_ *12));
        Assert.assertEquals(3, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(_192_MB_*12,solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        Assert.assertEquals(0,solver.getDeallocatedCapacityResult().getAgentUids().size());
        assertContainersPerAgent(solver, 5, 6);
        
        solver.reset();
        
        //scale-in to 1536 MB = 2 machines, 4 cores with 1536 MB
        solver.solveManualCapacityScaleIn(new AllocatedCapacity(Fraction.ZERO, _192_MB_*8));
        Assert.assertEquals(0, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(2, solver.getAllocatedCapacityForPu().getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO,_192_MB_*8),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        assertContainersPerAgent(solver, 4, 4);
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
        final int numberOfContainers = (int) (allocatedMemory / _192_MB_);
        return numberOfContainers;
    }
}
