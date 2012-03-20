package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractInstancesStatisticsConfig;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Defines that statistics are not aggregated but for a specific instance.
 * 
 * @author itaif
 * @since 9.0.0
 */
public class SingleInstanceStatisticsConfig extends AbstractInstancesStatisticsConfig {

    private String instanceUid;

    public SingleInstanceStatisticsConfig() {
        
    }
    
    public SingleInstanceStatisticsConfig(String instanceUid) {
        this.instanceUid = instanceUid;
    }

    /**
     * @see ProcessingUnitInstance#getUid()
     */
    public String getInstanceUid() {
        return instanceUid;
    }

    public void setInstanceUid(String instanceUid) {
        this.instanceUid = instanceUid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((instanceUid == null) ? 0 : instanceUid.hashCode());
        return result;
    }

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

    @Override
    public String toString() {
        return "singleInstanceStatistics {instanceUid=" + instanceUid + "}";
    }

    @Override
    public void validate() throws IllegalStateException {
        if (instanceUid == null) {
            throw new IllegalStateException("instance UID was not specified. Consider using " + EachSingleInstanceStatisticsConfig.class.getName() + " instead");
        }
    }

    @Override
    public Object getValue(StatisticsObjectList values) {
        throw new IllegalStateException("Unreachable Code");
    }
}
