package org.openspaces.grid.gsm.capacity;

import org.openspaces.core.internal.commons.math.fraction.Fraction;

/**
 * Capacity requirements used by the internal implementation of the Elastic Processing Unit management.
 * @author itaif
 *
 */
public class AllocatedCapacity {

    private final Fraction cpuCores;
    private final long memoryInMB;
    
    public AllocatedCapacity(Fraction cpuCores, long memoryInMB) {
        
        if (cpuCores.compareTo(Fraction.ZERO) < 0) {
            throw new IllegalArgumentException("cpuCores");
        }
        
        if (memoryInMB < 0) {
            throw new IllegalArgumentException("memoryInMB");
        }
        
        this.cpuCores =cpuCores;
        this.memoryInMB = memoryInMB;
    }
       
    
    public static AllocatedCapacity add(AllocatedCapacity allocation1, AllocatedCapacity allocation2) {
        
        return new AllocatedCapacity(
                allocation1.cpuCores.add(allocation2.cpuCores),
                allocation1.memoryInMB + allocation2.memoryInMB);
    }
    
    public static AllocatedCapacity subtract(AllocatedCapacity allocation1, AllocatedCapacity allocation2) {
        
        if (allocation1.memoryInMB - allocation2.memoryInMB < 0) {
            throw new IllegalArgumentException(allocation1 + " has more memory than existing " + allocation2);
        }
        
        if (allocation1.cpuCores.compareTo(allocation2.cpuCores) < 0) {
            throw new IllegalArgumentException(allocation1 + " has more cpu cores than existing " + allocation2);
        }
        
        return new AllocatedCapacity(
                allocation1.cpuCores.subtract(allocation2.cpuCores),
                allocation1.memoryInMB - allocation2.memoryInMB);
    }
    
    public static AllocatedCapacity subtractOrZero(AllocatedCapacity allocation1, AllocatedCapacity allocation2) {
        long memoryInMB = allocation1.memoryInMB - allocation2.memoryInMB;
        
        if (memoryInMB < 0) {
            memoryInMB = 0;
        }
        
        Fraction cpuCores = allocation1.cpuCores.subtract(allocation2.cpuCores);
        if (cpuCores.compareTo(Fraction.ZERO) < 0) {
            cpuCores = Fraction.ZERO;
        }
        
        return new AllocatedCapacity(cpuCores,memoryInMB);
    }
    
    public boolean equals(Object other) {
        return other instanceof AllocatedCapacity &&
               ((AllocatedCapacity)other).cpuCores.equals(cpuCores) &&
               ((AllocatedCapacity)other).memoryInMB == memoryInMB;
    }
    public boolean equalsZero() {
        // negative values are not allowed. Considered as zero.
        return isCpuCoresEqualsZero() && 
               isMemoryEqualsZero();
    }

    public boolean isCpuCoresEqualsZero() {
        return cpuCores.compareTo(Fraction.ZERO) <= 0;
    }


    public boolean isMemoryEqualsZero() {
        return memoryInMB == 0;
    }
    
    public String toString() {
        return 
            cpuCores + " cores and "  + 
            memoryInMB + "MB";
    }


    public boolean satisfies(AllocatedCapacity capacity) {
        
        if (capacity == null) {
            throw new NullPointerException("capacity");
        }
        
        return cpuCores.compareTo(capacity.cpuCores) >= 0 &&
               memoryInMB >= capacity.memoryInMB;
    }


    public boolean moreThanSatisfies(AllocatedCapacity capacity) {
        return cpuCores.compareTo(capacity.cpuCores) > 0 &&
        memoryInMB > capacity.memoryInMB;
    }


    public CapacityRequirements toCapacityRequirements() {
        return 
            new CapacityRequirements(
                new MemoryCapacityRequirment(memoryInMB),
                new CpuCapacityRequirement(cpuCores.doubleValue()));
    }


    public Fraction getCpuCores() {
        return cpuCores;
    }


    public long getMemoryInMB() {
        return memoryInMB;
    }


}

