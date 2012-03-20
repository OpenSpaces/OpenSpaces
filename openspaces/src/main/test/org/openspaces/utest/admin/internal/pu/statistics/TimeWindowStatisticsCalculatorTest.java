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
import java.util.Map;
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
import org.openspaces.admin.pu.statistics.MaximumTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.MinimumTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.PercentileTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;

/**
 * Tests TimeWindowStatisticsCalculator
 * @author itaif
 * @since 9.0.0
 */
public class TimeWindowStatisticsCalculatorTest extends TestCase {

    private static final int NUMBER_OF_SAMPLES = 4;
    private static final int SLEEP_MILLISECONDS = 1000;
    private static final int SLEEP_MILLISECONDS_WITH_JITTER = 1300;
    private static final int SLEEP_MILLISECONDS_WITH_TOO_MUCH_JITTER = 1900;
    private static final String INSTANCE_UID = "instanceUid";
    private static final String MONITOR = "monitor";
    private static final String METRIC = "metric";
    private static final long TIMEWINDOW_SECONDS = (long)(2*SLEEP_MILLISECONDS/1000.0);
    private static final long MAXIMUM_TIMEWINDOW_SECONDS = (long)(3*SLEEP_MILLISECONDS/1000.0);
    private static final long MINIMUM_TIMEWINDOW_SECONDS = 0;

    public void testSlidingWindowAverage() throws InterruptedException {
        boolean tooMuchJitter = false;
        slidingWindowAverage(SLEEP_MILLISECONDS, tooMuchJitter);
    }
    
    public void testSlidingWindowAverageWithSamplingJitter() throws InterruptedException {
        boolean tooMuchJitter = false;
        slidingWindowAverage(SLEEP_MILLISECONDS_WITH_JITTER, tooMuchJitter);
    }
    
    public void testSlidingWindowAverageWithSamplingTooMuchJitter() throws InterruptedException {
        boolean tooMuchJitter = true;
        slidingWindowAverage(SLEEP_MILLISECONDS_WITH_TOO_MUCH_JITTER, tooMuchJitter);
    }
    
    private void slidingWindowAverage(long millisecondsBetweenSamples, boolean tooMuchJitter) throws InterruptedException {
        TimeWindowStatisticsCalculator calculator = new TimeWindowStatisticsCalculator();
        int historySize = NUMBER_OF_SAMPLES;
        DefaultProcessingUnitStatisticsCalculatorFactory statisticsCalculatorFactory = new DefaultProcessingUnitStatisticsCalculatorFactory();
        ProcessingUnitStatistics lastStatistics = null;
        long now = System.currentTimeMillis();
        for (int i = 0 ; i < historySize ; i ++) {
            // create one new sample with value i
            long adminTimestamp = now + i * millisecondsBetweenSamples;
            InternalProcessingUnitStatistics processingUnitStatistics = new DefaultProcessingUnitStatistics(adminTimestamp , lastStatistics , historySize, statisticsCalculatorFactory);
            processingUnitStatistics.addStatistics(
                    lastSampleStatisticsId(), 
                    i);
            
            Assert.assertEquals(i, processingUnitStatistics.getStatistics().get(lastSampleStatisticsId()));
            if (i == 0) {
                Assert.assertNull(processingUnitStatistics.getPrevious());    
            }
            else {
                Assert.assertNotNull(processingUnitStatistics.getPrevious());
            }
            
            //calculate time average
            Set<ProcessingUnitStatisticsId> newStatisticsIds = new HashSet<ProcessingUnitStatisticsId>();
            newStatisticsIds.add(averageStatisticsId());
            newStatisticsIds.add(minimumStatisticsId());
            newStatisticsIds.add(maximumStatisticsId());
            newStatisticsIds.add(precentileStatisticsId(0));
            newStatisticsIds.add(precentileStatisticsId(1));
            newStatisticsIds.add(precentileStatisticsId(49));
            newStatisticsIds.add(precentileStatisticsId(50));
            newStatisticsIds.add(precentileStatisticsId(51));
            newStatisticsIds.add(precentileStatisticsId(99));
            newStatisticsIds.add(precentileStatisticsId(100));
            calculator.calculateNewStatistics(processingUnitStatistics, newStatisticsIds);
            lastStatistics = processingUnitStatistics;
        }
        
        Map<ProcessingUnitStatisticsId, Object> pppStatistics = lastStatistics.getPrevious().getPrevious().getPrevious().getStatistics();
        Map<ProcessingUnitStatisticsId, Object> ppStatistics = lastStatistics.getPrevious().getPrevious().getStatistics();
        Map<ProcessingUnitStatisticsId, Object> pStatistics = lastStatistics.getPrevious().getStatistics();
        Map<ProcessingUnitStatisticsId, Object> statistics = lastStatistics.getStatistics();
        
        if (!tooMuchJitter) {
            assertOneSampleStartingWith(0,pppStatistics);
            assertTwoSamplesStartingWith(0, ppStatistics);
            assertThreeSamplesStartingWith(0, pStatistics);
            assertThreeSamplesStartingWith(1, statistics);
        }
        else {
            assertOneSampleStartingWith(0,pppStatistics);
            assertTwoSamplesStartingWith(0, ppStatistics);
            assertTwoSamplesStartingWith(1, pStatistics);
            assertTwoSamplesStartingWith(2, statistics);
        }
        
        
    }

    private void assertThreeSamplesStartingWith(int firstValue, Map<ProcessingUnitStatisticsId, Object> pStatistics) {
        int secondValue = firstValue+1;
        int thirdValue = firstValue+2;
        Assert.assertEquals(thirdValue,         pStatistics.get(lastSampleStatisticsId()));
        Assert.assertEquals((firstValue+secondValue+thirdValue)/3.0,pStatistics.get(averageStatisticsId()));
        Assert.assertEquals(thirdValue,         pStatistics.get(maximumStatisticsId()));
        Assert.assertEquals(firstValue,         pStatistics.get(minimumStatisticsId()));
        Assert.assertEquals(firstValue,         pStatistics.get(precentileStatisticsId(0)));
        Assert.assertEquals(firstValue,         pStatistics.get(precentileStatisticsId(1)));
        Assert.assertEquals(secondValue,        pStatistics.get(precentileStatisticsId(49)));
        Assert.assertEquals(secondValue,        pStatistics.get(precentileStatisticsId(50)));
        Assert.assertEquals(secondValue,        pStatistics.get(precentileStatisticsId(51)));
        Assert.assertEquals(thirdValue,         pStatistics.get(precentileStatisticsId(99)));
        Assert.assertEquals(thirdValue,         pStatistics.get(precentileStatisticsId(100)));
    }

    private void assertTwoSamplesStartingWith(int firstValue, Map<ProcessingUnitStatisticsId, Object> ppStatistics) {
        int secondValue = firstValue+1;
        Assert.assertEquals(secondValue,         ppStatistics.get(lastSampleStatisticsId()));
        Assert.assertEquals((firstValue+secondValue)/2.0,  ppStatistics.get(averageStatisticsId()));
        Assert.assertEquals(secondValue,         ppStatistics.get(maximumStatisticsId()));
        Assert.assertEquals(firstValue,          ppStatistics.get(minimumStatisticsId()));
        Assert.assertEquals(firstValue,          ppStatistics.get(precentileStatisticsId(0)));
        Assert.assertEquals(firstValue,          ppStatistics.get(precentileStatisticsId(1)));
        Assert.assertEquals(firstValue,          ppStatistics.get(precentileStatisticsId(49)));
        Assert.assertEquals(secondValue,         ppStatistics.get(precentileStatisticsId(50)));
        Assert.assertEquals(secondValue,         ppStatistics.get(precentileStatisticsId(51)));
        Assert.assertEquals(secondValue,         ppStatistics.get(precentileStatisticsId(99)));
        Assert.assertEquals(secondValue,         ppStatistics.get(precentileStatisticsId(100)));
    }

    private void assertOneSampleStartingWith(int firstValue, Map<ProcessingUnitStatisticsId, Object> statistics) {
        
        Assert.assertEquals(firstValue,     statistics.get(lastSampleStatisticsId()));
        Assert.assertEquals(firstValue/1.0, statistics.get(averageStatisticsId()));
        Assert.assertEquals(firstValue,     statistics.get(maximumStatisticsId()));
        Assert.assertEquals(firstValue,     statistics.get(minimumStatisticsId()));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(0)));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(1)));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(49)));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(50)));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(51)));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(99)));
        Assert.assertEquals(firstValue,     statistics.get(precentileStatisticsId(100)));
    }

    private ProcessingUnitStatisticsId precentileStatisticsId(int i) {
        return new ProcessingUnitStatisticsIdConfigurer()
        .metric(METRIC)
        .monitor(MONITOR)
        .instancesStatistics(new SingleInstanceStatisticsConfig(INSTANCE_UID))
        .timeWindowStatistics(
                new PercentileTimeWindowStatisticsConfigurer()
                .percentile(i)
                .minimumTimeWindow(MINIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                .maximumTimeWindow(MAXIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                .timeWindow(TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                .create())
                .create();
    }
    
    private ProcessingUnitStatisticsId averageStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
                .metric(METRIC)
                .monitor(MONITOR)
                .instancesStatistics(new SingleInstanceStatisticsConfig(INSTANCE_UID))
                .timeWindowStatistics(
                        new AverageTimeWindowStatisticsConfigurer()
                        .minimumTimeWindow(MINIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .maximumTimeWindow(MAXIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .timeWindow(TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .create())
                 .create();
    }
    
    private ProcessingUnitStatisticsId minimumStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
                .metric(METRIC)
                .monitor(MONITOR)
                .instancesStatistics(new SingleInstanceStatisticsConfig(INSTANCE_UID))
                .timeWindowStatistics(
                        new MinimumTimeWindowStatisticsConfigurer()
                        .minimumTimeWindow(MINIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .maximumTimeWindow(MAXIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .timeWindow(TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .create())
                 .create();
    }

    private ProcessingUnitStatisticsId maximumStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
                .metric(METRIC)
                .monitor(MONITOR)
                .instancesStatistics(new SingleInstanceStatisticsConfig(INSTANCE_UID))
                .timeWindowStatistics(
                        new MaximumTimeWindowStatisticsConfigurer()
                        .minimumTimeWindow(MINIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .maximumTimeWindow(MAXIMUM_TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .timeWindow(TIMEWINDOW_SECONDS, TimeUnit.SECONDS)
                        .create())
                 .create();
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
