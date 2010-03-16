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
    
    private enum MemoryUnit {
        BYTES,
        KILOBYTES,
        MEGABYTES,
        GIGABYTES,
        TERABYTES
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
        String memoryUnitLowerCase = memoryUnit.toLowerCase();
        if (memoryUnitLowerCase.endsWith("b")) {
            return new MemorySettings(0, getMemorySize(memoryUnit));
        } else if (memoryUnitLowerCase.endsWith("k")) {
            return new MemorySettings(1, getMemorySize(memoryUnit));
        } else if (memoryUnitLowerCase.endsWith("m")) {
            return new MemorySettings(2, getMemorySize(memoryUnit));
        } else if (memoryUnitLowerCase.endsWith("g")) {
            return new MemorySettings(3, getMemorySize(memoryUnit));
        } else if (memoryUnitLowerCase.endsWith("t")) {
            return new MemorySettings(4, getMemorySize(memoryUnit));
        } else {
            throw new IllegalArgumentException("Unknown memory unit " + memoryUnit);
        }
    }
    
    private static int getMemorySize(String memoryUnit) {
        return Integer.valueOf(memoryUnit.substring(0, memoryUnit.length() - 1));
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
    
    public boolean isGreaterThan(MemorySettings other) {
        double convertedSize = doConvert(other.index - index, other.size);
        return this.size > convertedSize;
    }

    public int toB() {
        return (int)doConvert(index - MemoryUnit.BYTES.ordinal(), size);
    }
    
    public int toMB() {
        return (int)doConvert(index - MemoryUnit.MEGABYTES.ordinal(), size);
    }
    
    public int toKB() {
        return (int)doConvert(index - MemoryUnit.KILOBYTES.ordinal(), size);
    }
    
    public int toGB() {
        return (int)doConvert(index - MemoryUnit.GIGABYTES.ordinal(), size);
    }
    
    public int toTB() {
        return (int)doConvert(index - MemoryUnit.TERABYTES.ordinal(), size);
    }
}
