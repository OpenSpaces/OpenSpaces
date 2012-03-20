package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractInstancesStatisticsConfig;

/**
 * Picks the Nth percentile of cluster instance values.
 * For example 50th percentile is the median.
 * @since 9.0.0
 * @author itaif
 *
 */
public class PercentileInstancesStatisticsConfig extends AbstractInstancesStatisticsConfig {

    private double percentile;
    
    public double getPercentile() {
        return percentile;
    }

    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(percentile);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        PercentileInstancesStatisticsConfig other = (PercentileInstancesStatisticsConfig) obj;
        if (Double.doubleToLongBits(percentile) != Double.doubleToLongBits(other.percentile))
            return false;
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "percentileInstancesStatistics {percentile=" + percentile + "}";
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalInstancesStatisticsConfig#validate()
     */
    @Override
    public void validate() throws IllegalStateException {
        if (percentile <0 || percentile > 100) {
            throw new IllegalArgumentException("percentile ("+percentile+") must between 0 and 100 (inclusive)");
        }
    }

}
