package org.openspaces.admin.esm.deployment;

/**
 * Memory Service Level Agreement specifying the threshold to trigger memory breach.
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class MemorySla implements SLA {
    
    private final int threshold;

    public MemorySla(String threshold) {
        if (!threshold.endsWith("%")) {
            throw new IllegalArgumentException("Memory SLA argument should end with a precentail; e.g. 70%");
        }
        String value = threshold.substring(0, threshold.length() -1);
        this.threshold = Integer.valueOf(value);
    }
    
    public int getThreshold() {
        return threshold;
    }
}
