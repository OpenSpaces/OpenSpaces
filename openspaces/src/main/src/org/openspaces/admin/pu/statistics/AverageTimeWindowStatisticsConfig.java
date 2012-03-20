package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.AbstractTimeWindowStatisticsConfig;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;

public class AverageTimeWindowStatisticsConfig extends AbstractTimeWindowStatisticsConfig {

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.admin.InternalProcessingUnitStatisticsCalculatorFactory#createProcessingUnitStatisticsCalculator()
     */
    @Override
    public Class<? extends InternalProcessingUnitStatisticsCalculator> getProcessingUnitStatisticsCalculator() {
        return TimeWindowStatisticsCalculator.class;
    }
}
