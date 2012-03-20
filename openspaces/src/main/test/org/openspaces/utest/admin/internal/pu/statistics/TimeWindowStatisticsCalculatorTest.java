/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.utest.admin.internal.pu.statistics;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.admin.internal.pu.DefaultProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.DefaultProcessingUnitStatisticsCalculatorFactory;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.TimeWindowStatisticsCalculator;
import org.openspaces.admin.pu.statistics.AverageTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;

/**
 * @author itaif
 *
 */
public class TimeWindowStatisticsCalculatorTest extends TestCase {

    private static final String INSTANCE_UID = "instanceUid";
    private static final String MONITOR = "monitor";
    private static final String METRIC = "metric";
    private static final long TIMEWINDOW_SECONDS = 60000;
    private static final long MINIMUM_TIMEWINDOW_NANOSECONDS = 1;

    public void testAverage() throws InterruptedException {
        TimeWindowStatisticsCalculator calculator = new TimeWindowStatisticsCalculator();
        int historySize = 3;
        DefaultProcessingUnitStatisticsCalculatorFactory statisticsCalculatorFactory = new DefaultProcessingUnitStatisticsCalculatorFactory();
        ProcessingUnitStatistics lastStatistics = null;
        for (int i = 0 ; i < historySize ; i ++) {
            // create one new sample with value i
            long adminTimestamp = System.currentTimeMillis();
            InternalProcessingUnitStatistics processingUnitStatistics = new DefaultProcessingUnitStatistics(adminTimestamp , lastStatistics , historySize, statisticsCalculatorFactory);
            processingUnitStatistics.addStatistics(
                    lastSampleStatisticsId(), 
                    i);
            
            Assert.assertEquals(i, processingUnitStatistics.getStatistics().get(lastSampleStatisticsId()));
            
            //calculate time average
            Set<ProcessingUnitStatisticsId> newStatisticsIds = new HashSet<ProcessingUnitStatisticsId>();
            newStatisticsIds.add(
                    averageStatisticsId()
                    .create());
            calculator.calculateNewStatistics(processingUnitStatistics, newStatisticsIds);
            lastStatistics = processingUnitStatistics;
            
            //next time sample
            Thread.sleep(100);
        }
        
        Assert.assertEquals(2, lastStatistics.getStatistics().get(lastSampleStatisticsId()));
        Assert.assertEquals(1, lastStatistics.getPrevious().getStatistics().get(lastSampleStatisticsId()));
        Assert.assertEquals(0, lastStatistics.getPrevious().getPrevious().getStatistics().get(lastSampleStatisticsId()));
        
    }

    private ProcessingUnitStatisticsIdConfigurer averageStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
        .metric(METRIC)
        .monitor(MONITOR)
        .instancesStatistics(new SingleInstanceStatisticsConfig(INSTANCE_UID))
        .timeWindowStatistics(
                new AverageTimeWindowStatisticsConfigurer()
                .minimumTimeWindow(MINIMUM_TIMEWINDOW_NANOSECONDS, TimeUnit.NANOSECONDS)
                .timeWindow(TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                .create());
    }

    private ProcessingUnitStatisticsId lastSampleStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
        .metric(METRIC)
        .monitor(MONITOR)
        .instancesStatistics(new SingleInstanceStatisticsConfig(INSTANCE_UID))
        .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
        .create();
    }
}
