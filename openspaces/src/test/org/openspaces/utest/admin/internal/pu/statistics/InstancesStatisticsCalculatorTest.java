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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InstancesStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.pu.statistics.AverageInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.MaximumInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.MinimumInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.PercentileInstancesStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfig;

/**
 * Unit Tests for {@link InstancesStatisticsCalculator}
 * @author itaif
 * @since 9.0.0
 */
public class InstancesStatisticsCalculatorTest extends TestCase {

    private static final String INSTANCE_UID0 = "instanceUid0";
    private static final String INSTANCE_UID1 = "instanceUid1";
    private static final String MONITOR = "monitor";
    private static final String METRIC = "metric";
    
    @Test
    public void test() {
        InstancesStatisticsCalculator calculator = new InstancesStatisticsCalculator();

        ProcessingUnitStatistics lastStatistics = null;
        long now = System.currentTimeMillis();
    
        int historySize = 1;
        InternalProcessingUnitStatistics processingUnitStatistics = 
                new DefaultProcessingUnitStatistics(now , lastStatistics , historySize);
        
        processingUnitStatistics.addStatistics(
                    lastSampleStatisticsId(INSTANCE_UID0), 
                    0);
        
        processingUnitStatistics.addStatistics(
                lastSampleStatisticsId(INSTANCE_UID1), 
                1);
    
        calculator.calculateNewStatistics(processingUnitStatistics, getTestStatisticsCalculations());
        
        Assert.assertEquals(0, processingUnitStatistics.getStatistics().get(lastSampleStatisticsId(INSTANCE_UID0)));
        Assert.assertEquals(1, processingUnitStatistics.getStatistics().get(lastSampleStatisticsId(INSTANCE_UID1)));
        Assert.assertEquals(0, processingUnitStatistics.getStatistics().get(minimumStatisticsId()));
        Assert.assertEquals(1, processingUnitStatistics.getStatistics().get(maximumStatisticsId()));
        Assert.assertEquals(0.5, processingUnitStatistics.getStatistics().get(averageStatisticsId()));
        
    }

    private ProcessingUnitStatisticsId lastSampleStatisticsId(String instanceUid) {
        return new ProcessingUnitStatisticsIdConfigurer()
                .metric(METRIC)
                .monitor(MONITOR)
                .instancesStatistics(new SingleInstanceStatisticsConfigurer().instanceUid(instanceUid).create())
                .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
                .agentZones(new ExactZonesConfig())
                .create();
    }

    private Collection<ProcessingUnitStatisticsId> getTestStatisticsCalculations() {
        List<ProcessingUnitStatisticsId> newStatisticsIds = new ArrayList<ProcessingUnitStatisticsId>();
        newStatisticsIds.add(averageStatisticsId());
        newStatisticsIds.add(minimumStatisticsId());
        newStatisticsIds.add(maximumStatisticsId());
        newStatisticsIds.add(precentileStatisticsId(0));
        newStatisticsIds.add(precentileStatisticsId(49));
        newStatisticsIds.add(precentileStatisticsId(50));
        newStatisticsIds.add(precentileStatisticsId(51));
        newStatisticsIds.add(precentileStatisticsId(100));
        return newStatisticsIds;
    }

    private ProcessingUnitStatisticsId precentileStatisticsId(double percentile) {
    
        return new ProcessingUnitStatisticsIdConfigurer()
               .monitor(MONITOR)
               .metric(METRIC)
               .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
               .instancesStatistics(new PercentileInstancesStatisticsConfigurer().percentile(percentile).create())
               .agentZones(new ExactZonesConfig())
               .create();

    }

    private ProcessingUnitStatisticsId averageStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
               .monitor(MONITOR)
               .metric(METRIC)
               .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
               .instancesStatistics(new AverageInstancesStatisticsConfig())
               .agentZones(new ExactZonesConfig())
               .create();
    }
    
    private ProcessingUnitStatisticsId minimumStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
               .monitor(MONITOR)
               .metric(METRIC)
               .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
               .instancesStatistics(new MinimumInstancesStatisticsConfig())
               .agentZones(new ExactZonesConfig())
               .create();
    }
    
    private ProcessingUnitStatisticsId maximumStatisticsId() {
        return new ProcessingUnitStatisticsIdConfigurer()
               .monitor(MONITOR)
               .metric(METRIC)
               .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
               .instancesStatistics(new MaximumInstancesStatisticsConfig())
               .agentZones(new ExactZonesConfig())
               .create();
    }
    
}
