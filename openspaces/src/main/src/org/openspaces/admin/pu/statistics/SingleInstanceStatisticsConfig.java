package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Defines that statistics are not aggregated but for a specific instance.
 * 
 * @author itaif
 * @since 9.0.0
 */
public class SingleInstanceStatisticsConfig extends InstancesStatisticsConfig {

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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((processingUnitInstanceUid == null) ? 0 : processingUnitInstanceUid.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SingleInstanceStatisticsConfig other = (SingleInstanceStatisticsConfig) obj;
        if (processingUnitInstanceUid == null) {
            if (other.processingUnitInstanceUid != null)
                return false;
        } else if (!processingUnitInstanceUid.equals(other.processingUnitInstanceUid))
            return false;
        return true;
    }
    
}
