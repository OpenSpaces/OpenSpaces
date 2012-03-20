package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractTimeWindowStatisticsConfig;
import org.openspaces.admin.internal.pu.statistics.DoNothingProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;

public class LastSampleTimeWindowStatisticsConfig extends AbstractTimeWindowStatisticsConfig {

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "lastSampleTimeWindowStatistics";
    }
    
    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.admin.InternalProcessingUnitStatisticsCalculatorFactory#createProcessingUnitStatisticsCalculator()
     */
    @Override
    public InternalProcessingUnitStatisticsCalculator createProcessingUnitStatisticsCalculator() {
        return new DoNothingProcessingUnitStatisticsCalculator();
    }

}
