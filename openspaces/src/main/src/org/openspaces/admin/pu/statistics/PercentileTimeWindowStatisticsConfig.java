package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;


public class PercentileTimeWindowStatisticsConfig  
        extends AbstractTimeWindowStatisticsConfig
        implements StatisticsObjectListFunction {

    private Double percentile;
        
    public Double getPercentile() {
        return percentile;
    }
    
    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    @Override
    public void validate() throws IllegalStateException {
        super.validate();
        if (percentile == null) {
            throw new IllegalArgumentException("percentile cannot be null");
        }
        
        if (percentile <0 || percentile > 100) {
            throw new IllegalArgumentException("percentile ("+percentile+") must between 0 and 100 (inclusive)");
        }
    }

    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getPercentile(percentile);
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((percentile == null) ? 0 : percentile.hashCode());
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
        PercentileTimeWindowStatisticsConfig other = (PercentileTimeWindowStatisticsConfig) obj;
        if (percentile == null) {
            if (other.percentile != null)
                return false;
        } else if (!percentile.equals(other.percentile))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "percentile { percentile="+percentile+" , timeWindowSeconds="+getTimeWindowSeconds() + ", minimumTimeWindowSeconds="+getMinimumTimeWindowSeconds() + ", maximumTimeWindowSeconds="+ getMaximumTimeWindowSeconds()+"}";
    }    
}
