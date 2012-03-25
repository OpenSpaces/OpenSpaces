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
package org.openspaces.utest.grid.gsm;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfigurer;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementConfigurer;
import org.openspaces.admin.pu.statistics.AverageInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.AverageTimeWindowStatisticsConfigurer;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.core.util.MemoryUnit;

/**
 * Tests the scale strategy config object to key/value pair conversions. 
 * @author itaif
 * @since 9.0.0
 */
public class AutomaticCapacityScaleConfigurerTest extends TestCase {
    
    public void test() {

        ProcessingUnitStatisticsId statisticsId = 
             new ProcessingUnitStatisticsIdConfigurer()
            .monitor("")
            .metric("")
            .timeWindowStatistics(new AverageTimeWindowStatisticsConfigurer().timeWindow(1, TimeUnit.MINUTES).create())
            .instancesStatistics(new AverageInstancesStatisticsConfig())
            .create();
        
        ProcessingUnitStatisticsId dupStatisticsId = new ProcessingUnitStatisticsId(statisticsId.getProperties());
        Assert.assertEquals(statisticsId.getMonitor(), dupStatisticsId.getMonitor());
        Assert.assertEquals(statisticsId.getMetric(), dupStatisticsId.getMetric());
        Assert.assertEquals(statisticsId.getTimeWindowStatistics(), dupStatisticsId.getTimeWindowStatistics());
        Assert.assertEquals(statisticsId.getInstancesStatistics(), dupStatisticsId.getInstancesStatistics());
        Assert.assertEquals(statisticsId, dupStatisticsId);
        
                
        AutomaticCapacityScaleRuleConfig rule = 
        new AutomaticCapacityScaleRuleConfigurer()
        .statistics(statisticsId)
        .lowThreshold(1)
        .highThreshold(10)
        
//        .lowThresholdCapacityDecrease( // or increase
//                new CapacityRequirementConfigurer()
//                .memoryCapacity(1,MemoryUnit.GIGABYTES)
//                .create())
//                
//        .highThresholdCapacityIncrease(
//                new CapacityRequirementConfigurer()
//                .memoryCapacity(1,MemoryUnit.GIGABYTES)
//                .create())
//              
        .create();
      
        AutomaticCapacityScaleRuleConfig dupRule = new AutomaticCapacityScaleRuleConfig(rule.getProperties());
        Assert.assertEquals(statisticsId,dupRule.getStatistics());
        Assert.assertEquals(1.0,dupRule.getLowThreshold());
        Assert.assertEquals(10.0,dupRule.getHighThreshold());
                
         CapacityRequirementConfig capacity = new CapacityRequirementConfigurer()
        .memoryCapacity(1,MemoryUnit.GIGABYTES)
        .create();
        AutomaticCapacityScaleConfig config = new AutomaticCapacityScaleConfigurer()
                        .statisticsPollingInterval(1, TimeUnit.MINUTES)
                        .minCapacity(capacity)
                        .maxCapacity(capacity)
                        .cooldownAfterInstanceAdded(3, TimeUnit.MINUTES)
                        .cooldownAfterInstanceRemoved(1, TimeUnit.MINUTES)
                        //.addRule(rule)
                        .create();

        AutomaticCapacityScaleConfig dupConfig = new AutomaticCapacityScaleConfig(config.getProperties());
        Assert.assertEquals(config, dupConfig);
        Assert.assertEquals(dupConfig.getStatisticsPollingIntervalSeconds(),1*60);
        Assert.assertEquals(dupConfig.getMinCapacity(),capacity);
        Assert.assertEquals(dupConfig.getCooldownAfterInstanceAddedSeconds(),3*60);
        Assert.assertEquals(dupConfig.getCooldownAfterInstanceRemovedSeconds(),1*60);
    }
}
