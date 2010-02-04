package org.openspaces.admin.esm.deployment;

public class MemorySla implements SLA {
    
    private final int threshold;

    public MemorySla(int threshold) {
        this.threshold = threshold;
    }
    
    public int getThreshold() {
        return threshold;
    }
}
