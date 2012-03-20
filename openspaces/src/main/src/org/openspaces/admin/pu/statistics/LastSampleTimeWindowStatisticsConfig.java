package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.DoNothingProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalTimeWindowStatisticsConfig;

public class LastSampleTimeWindowStatisticsConfig implements InternalTimeWindowStatisticsConfig {
   
    @Override
    public Class<? extends InternalProcessingUnitStatisticsCalculator> getProcessingUnitStatisticsCalculator() {
        return DoNothingProcessingUnitStatisticsCalculator.class;
    }

    @Override
    public void validate() throws IllegalStateException {
        // ok
    }

}
