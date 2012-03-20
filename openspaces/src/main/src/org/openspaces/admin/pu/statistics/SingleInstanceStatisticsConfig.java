package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Defines that statistics are not aggregated but for a specific instance.
 * 
 * @author itaif
 * @since 9.0.0
 */
public class SingleInstanceStatisticsConfig extends InstancesStatisticsConfig {

    private String instanceUid;

    /**
     * @see ProcessingUnitInstance#getUid()
     * @return
     */
    public String getInstanceUid() {
        return instanceUid;
    }

    public void setInstanceUid(String instanceUid) {
        this.instanceUid = instanceUid;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((instanceUid == null) ? 0 : instanceUid.hashCode());
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
        if (instanceUid == null) {
            if (other.instanceUid != null)
                return false;
        } else if (!instanceUid.equals(other.instanceUid))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "singleInstanceStatisticsConfig {instanceUid=" + instanceUid + "}";
    }
}
