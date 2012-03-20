package org.openspaces.admin.pu.statistics;

public abstract class InstancesStatisticsConfig {

    /* Default implementation for configs without members
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
    
    /* Default implementation for configs without members
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }
}
