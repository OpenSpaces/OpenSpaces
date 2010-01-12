package org.openspaces.grid.esm;

public class MemorySettings {

    /** the index of this unit */
    private final int index;
    /** the memory size */
    private final int size;

    /** Internal constructor */
    MemorySettings(int index, int size) { 
        this.index = index;
        this.size = size; 
    }
    
    /** Lookup table for conversion factors */
    private static final int[] multipliers = { 
        1, 
        1024, 
        1024 * 1024, 
        1024 * 1024 * 1024,
        1024 * 1024 * 1024 * 1024
    };
    
    /**
     * Perform conversion based on given delta representing the
     * difference between units
     * @param delta the difference in index values of source and target units
     * @param size the value to convert
     * @return converted duration or saturated value
     */
    private static double doConvert(int delta, int size) {
        if (delta == 0)
            return size;
        if (delta < 0) 
            return (1.0 * size / multipliers[-delta]);

        return (1.0 * size * multipliers[delta]);
    }
    
    public static MemorySettings valueOf(String memoryUnit) {
        if (memoryUnit.endsWith("KB")) {
            return new MemorySettings(1, getMemorySize(memoryUnit, 2));
        } else if (memoryUnit.endsWith("MB")) {
            return new MemorySettings(2, getMemorySize(memoryUnit, 2));
        } else if (memoryUnit.endsWith("GB")) {
            return new MemorySettings(3, getMemorySize(memoryUnit, 2));
        } else if (memoryUnit.endsWith("TB")) {
            return new MemorySettings(4, getMemorySize(memoryUnit, 2));
        } else if (memoryUnit.endsWith("B")) {
            return new MemorySettings(0, getMemorySize(memoryUnit, 1));
        } else {
            throw new IllegalArgumentException("Unknown memory unit " + memoryUnit);
        }
    }
    
    private static int getMemorySize(String memoryUnit, int unit) {
        return Integer.valueOf(memoryUnit.substring(0, memoryUnit.length() - unit));
    }
    
    public double dividedBy(String memoryUnit) {
        MemorySettings other = MemorySettings.valueOf(memoryUnit);
        double convertedSize = doConvert(other.index - index, other.size);
        double result = (this.size / convertedSize);
        return result;
    }
    
    public int floorDividedBy(String memoryUnit) {
        double result = Math.floor(dividedBy(memoryUnit));
        return (int)result;
    }
    
    public int ceilDividedBy(String memoryUnit) {
        double result =  Math.ceil(dividedBy(memoryUnit));
        return (int)result;
    }
    
    public static void main(String[] args) {
        
        String minMemory = "1GB";
        String maxMemory = "10GB";
        String jvmSize = "512MB";
        
        System.out.println("initial jvms: " + MemorySettings.valueOf(minMemory).dividedBy(jvmSize));
        System.out.println("num of partitions: " + MemorySettings.valueOf(maxMemory).dividedBy(jvmSize));
    }
}
