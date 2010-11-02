package org.openspaces.admin.pu.elastic;

/**
 * Memory Service Level Agreement (SLA) specifying the threshold to trigger memory breach.
 * Threshold is breached if memory usage percentage is above or below the specified threshold. 
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going through the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 */
public class MemorySla implements SLA {
    
    private final int threshold;
    private final int subsetSize;

    /**
     * An SLA with a memory threshold percentage and a default moving average subset size of 6 (Six
     * samples five seconds apart, giving a total of a 30 seconds window).
     * 
     * @param threshold
     *            A memory usage percentage (e.g. 75%).
     */
    public MemorySla(String threshold) {
        this(threshold, 6);
    }

    /**
     * An SLA with a memory threshold percentage and a given moving average subset size (samples are
     * five seconds apart, giving a total of a subsetSize*5 seconds window).
     * 
     * @param threshold
     *            A memory usage percentage (e.g. 75%).
     * @param subsetSize
     *            A subset size for calculating a moving average of memory statistics.
     * 
     */
    public MemorySla(String threshold, int subsetSize) {
        if (!threshold.endsWith("%")) {
            throw new IllegalArgumentException("Memory SLA argument should end with a precentail; e.g. 70%");
        }
        String value = threshold.substring(0, threshold.length() -1);
        this.threshold = Integer.valueOf(value);
        this.subsetSize = subsetSize;
    }
    
    /**
     * Returns the threshold integer value.
     * @return threshold integer value.
     */
    public int getThreshold() {
        return threshold;
    }
    
    /**
     * Returns the moving average subset size.
     * @return a subset site integer value.
     */
    public int getSubsetSize() {
        return subsetSize;
    }
}
