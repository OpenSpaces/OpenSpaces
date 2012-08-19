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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.admin.internal.pu.DefaultProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.ProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.pu.statistics.AverageInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.AverageTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.LastSampleTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfigurer;
import org.openspaces.admin.zone.config.AtLeastOneZoneStatisticsConfigurer;
import org.openspaces.admin.zone.config.ExactZonesStatisticsConfigurer;

/**
 * @author elip
 *
 */
public class ZoneStatisticsCalculatorTest extends TestCase {

    private static final int TIME_WINDOW_SECONDS = 5;
    private static final int METRIC_VALUE = 5;

    private static final String INSTANCE_UID0 = "instanceUid0";

    private static final String MONITOR = "monitor";
    private static final String METRIC = "metric";

    private static final String ZONE_1 = "zone1";
    private static final String ZONE_2 = "zone2";

    public void test() {

        int historySize = 2;
        long now = System.currentTimeMillis();
        long prev = now - TimeUnit.SECONDS.toMillis(TIME_WINDOW_SECONDS);
        
        ProcessingUnitStatistics prevPrevProcessingUnitStatistics = null;
        
        InternalProcessingUnitStatistics prevProcessingUnitStatistics = 
                new DefaultProcessingUnitStatistics(prev, prevPrevProcessingUnitStatistics , historySize);
        prevProcessingUnitStatistics.addStatistics(
                lastTimeSampleStatisticsId(INSTANCE_UID0), 
                METRIC_VALUE);
        
        InternalProcessingUnitStatistics processingUnitStatistics = 
                new DefaultProcessingUnitStatistics(now , prevProcessingUnitStatistics , historySize);
        processingUnitStatistics.addStatistics(
                lastTimeSampleStatisticsId(INSTANCE_UID0), 
                METRIC_VALUE);
        
        processingUnitStatistics.calculateStatistics(getTestStatisticsCalculations());
        
        Assert.assertEquals((double)METRIC_VALUE, processingUnitStatistics.getStatistics().get(zoneStatisticsId()));

    }
    
    private ProcessingUnitStatisticsId lastTimeSampleStatisticsId(String instanceUid) {

        HashSet<String> zones = new HashSet<String>();
        zones.add(ZONE_1);
        zones.add(ZONE_2);
        
        return new ProcessingUnitStatisticsIdConfigurer()
            .metric(METRIC)
            .monitor(MONITOR)
            .instancesStatistics(new SingleInstanceStatisticsConfigurer().instanceUid(instanceUid).create())
            .timeWindowStatistics(new LastSampleTimeWindowStatisticsConfig())
            .zoneStatistics(new ExactZonesStatisticsConfigurer().zones(zones).create())
            .create();

        
    }
    
    private Collection<ProcessingUnitStatisticsId> getTestStatisticsCalculations() {
        
        List<ProcessingUnitStatisticsId> newStatisticsIds = new ArrayList<ProcessingUnitStatisticsId>();
        newStatisticsIds.add(zoneStatisticsId());
        
        return newStatisticsIds;
    }
    
   private ProcessingUnitStatisticsId zoneStatisticsId() {
        
        return new ProcessingUnitStatisticsIdConfigurer()
            .monitor(MONITOR)
            .metric(METRIC)
            .timeWindowStatistics(new AverageTimeWindowStatisticsConfigurer().timeWindow(TIME_WINDOW_SECONDS, TimeUnit.SECONDS).create())
            .instancesStatistics(new AverageInstancesStatisticsConfig())
            .zoneStatistics(new AtLeastOneZoneStatisticsConfigurer().zone(ZONE_1).create())
            .create();
    }
}
