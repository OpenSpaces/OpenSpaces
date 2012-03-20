package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * 
 * @author itaif
 * @since 9.0.0
 */
public class SingleInstanceStatisticsConfig extends InstancesAggregationStatisticsConfig {

    private String processingUnitInstanceUid;

    /**
     * @see ProcessingUnitInstance#getUid()
     * @return
     */
    public String getProcessingUnitInstanceUid() {
        return processingUnitInstanceUid;
    }

    public void setProcessingUnitInstanceUid(String processingUnitInstanceUid) {
        this.processingUnitInstanceUid = processingUnitInstanceUid;
    }
}
