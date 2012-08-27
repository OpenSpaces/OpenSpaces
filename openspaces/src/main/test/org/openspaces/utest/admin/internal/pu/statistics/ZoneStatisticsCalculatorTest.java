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
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.pu.statistics.AverageInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.AverageTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfigurer;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;

/**
 * @author elip
 *
 */
public class ZoneStatisticsCalculatorTest extends TestCase {

    private static final int TIME_WINDOW_SECONDS = 5;
    private static final double EXPECTED_METRIC_VALUE = 5;
    private static final double METRIC_VALUE_UID0 = EXPECTED_METRIC_VALUE + 1;
    private static final double METRIC_VALUE_UID1 = EXPECTED_METRIC_VALUE - 1;

    private static final String INSTANCE_UID0 = "instanceUid0";
    private static final String INSTANCE_UID1 = "instanceUid1";

    private static final String MONITOR = "monitor";
    private static final String METRIC = "metric";

    private static final String ZONE_1 = "zone1";
    private static final String ZONE_2 = "zone2";
    
    private static final int historySize = 2;
    
    @Test
    public void test() {

        InternalProcessingUnitStatistics processingUnitStatistics = createProcessingUnitStatistics();
        
        List<ProcessingUnitStatisticsId> newStatisticsIds = new ArrayList<ProcessingUnitStatisticsId>();
        newStatisticsIds.add(atLeastOneZoneStatisticsId());
        processingUnitStatistics.calculateStatistics(newStatisticsIds);
        
        Assert.assertEquals(EXPECTED_METRIC_VALUE, processingUnitStatistics.getStatistics().get(atLeastOneZoneStatisticsId()));
    }
    
    
    @Test
    public void testSimilarStatistics() {
        
        InternalProcessingUnitStatistics processingUnitStatistics = createProcessingUnitStatistics();
        
        List<ProcessingUnitStatisticsId> newStatisticsIds = new ArrayList<ProcessingUnitStatisticsId>();
        newStatisticsIds.add(atLeastOneZoneStatisticsId());
        newStatisticsIds.add(exactZonesStatisticsId());
        processingUnitStatistics.calculateStatistics(newStatisticsIds);
        
        Assert.assertEquals(EXPECTED_METRIC_VALUE, processingUnitStatistics.getStatistics().get(atLeastOneZoneStatisticsId()));
        Assert.assertEquals(EXPECTED_METRIC_VALUE, processingUnitStatistics.getStatistics().get(exactZonesStatisticsId()));
        
    }

    private InternalProcessingUnitStatistics createProcessingUnitStatistics() {
        ProcessingUnitStatistics prevPrevProcessingUnitStatistics = null;
        
        final long now = System.currentTimeMillis();
        final long prev = now - TimeUnit.SECONDS.toMillis(TIME_WINDOW_SECONDS);
        
        InternalProcessingUnitStatistics prevProcessingUnitStatistics = 
                new DefaultProcessingUnitStatistics(prev, prevPrevProcessingUnitStatistics , historySize);
        prevProcessingUnitStatistics.addStatistics(
                lastTimeSampleStatisticsId(INSTANCE_UID0), 
                METRIC_VALUE_UID0);
        prevProcessingUnitStatistics.addStatistics(
                lastTimeSampleStatisticsId(INSTANCE_UID1), 
                METRIC_VALUE_UID1);
        
        InternalProcessingUnitStatistics processingUnitStatistics = 
                new DefaultProcessingUnitStatistics(now , prevProcessingUnitStatistics , historySize);
        processingUnitStatistics.addStatistics(
                lastTimeSampleStatisticsId(INSTANCE_UID0), 
                METRIC_VALUE_UID0);
        processingUnitStatistics.addStatistics(
                lastTimeSampleStatisticsId(INSTANCE_UID1), 
                METRIC_VALUE_UID1);
        return processingUnitStatistics;
    }
    
    private ProcessingUnitStatisticsId lastTimeSampleStatisticsId(String instanceUid) {

        return new ProcessingUnitStatisticsIdConfigurer()
            .metric(METRIC)
            .monitor(MONITOR)
            .instancesStatistics(new SingleInstanceStatisticsConfigurer().instanceUid(instanceUid).create())
            .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
            .agentZones(new ExactZonesConfigurer().addZones(ZONE_1,ZONE_2).create())
            .create();

        
    }
    
   private ProcessingUnitStatisticsId atLeastOneZoneStatisticsId() {
        
        return new ProcessingUnitStatisticsIdConfigurer()
            .monitor(MONITOR)
            .metric(METRIC)
            .timeWindowStatistics(new AverageTimeWindowStatisticsConfigurer().timeWindow(TIME_WINDOW_SECONDS, TimeUnit.SECONDS).create())
            .instancesStatistics(new AverageInstancesStatisticsConfig())
            .agentZones(new AtLeastOneZoneConfigurer().addZone(ZONE_1).create())
            .create();
    }
   
   private ProcessingUnitStatisticsId exactZonesStatisticsId() {
       
       return new ProcessingUnitStatisticsIdConfigurer()
           .monitor(MONITOR)
           .metric(METRIC)
           .timeWindowStatistics(new AverageTimeWindowStatisticsConfigurer().timeWindow(TIME_WINDOW_SECONDS, TimeUnit.SECONDS).create())
           .instancesStatistics(new AverageInstancesStatisticsConfig())
           .agentZones(new ExactZonesConfigurer().addZone(ZONE_1).create())
           .create();
   }
}
