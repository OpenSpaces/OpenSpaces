/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpaceStatistics;

/**
 * @author kimchy
 */
public class DefaultSpaceStatistics implements SpaceStatistics {

    private final long timestamp;

    private final SpaceInstanceStatistics[] stats;
    
    private volatile SpaceStatistics previous;

    public DefaultSpaceStatistics(SpaceInstanceStatistics[] stats, SpaceStatistics previous, int historySize) {
        this.stats = stats;
        this.timestamp = System.currentTimeMillis();
        this.previous = previous;

        SpaceStatistics lastStats = previous;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultSpaceStatistics) lastStats).setPrevious(null);
        }
    }

    @Override
    public boolean isNA() {
        return stats == null || stats.length == 0 || stats[0].isNA();
    }

    @Override
    public SpaceStatistics getPrevious() {
        return previous;
    }

    public void setPrevious(SpaceStatistics previous) {
        this.previous = previous;
    }

    @Override
    public int getSize() {
        return stats.length;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public long getWriteCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getWriteCount();
            }
        }
        return total;
    }

    @Override
    public double getWritePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getWritePerSecond();
            }
        }
        return total;
    }

    @Override
    public long getReadCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getReadCount();
            }
        }
        return total;
    }

    @Override
    public double getReadPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getReadPerSecond();
            }
        }
        return total;
    }

    @Override
    public long getTakeCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getTakeCount();
            }
        }
        return total;
    }

    @Override
    public double getTakePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getTakePerSecond();
            }
        }
        return total;
    }

    @Override
    public long getNotifyRegistrationCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyRegistrationCount();
            }
        }
        return total;
    }

    @Override
    public double getNotifyRegistrationPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyRegistrationPerSecond();
            }
        }
        return total;
    }

    @Override
    public long getCleanCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getCleanCount();
            }
        }
        return total;
    }

    @Override
    public double getCleanPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getCleanPerSecond();
            }
        }
        return total;
    }

    @Override
    public long getUpdateCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getUpdateCount();
            }
        }
        return total;
    }

    @Override
    public double getUpdatePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getUpdatePerSecond();
            }
        }
        return total;
    }

    @Override
    public long getNotifyTriggerCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyTriggerCount();
            }
        }
        return total;
    }

    @Override
    public double getNotifyTriggerPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyRegistrationPerSecond();
            }
        }
        return total;
    }

    @Override
    public long getNotifyAckCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyAckCount();
            }
        }
        return total;
    }

    @Override
    public double getNotifyAckPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyAckPerSecond();
            }
        }
        return total;
    }

    @Override
    public long getExecuteCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getExecuteCount();
            }
        }
        return total;
    }

    @Override
    public double getExecutePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getExecutePerSecond();
            }
        }
        return total;
    }

    @Override
    public long getRemoveCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getRemoveCount();
            }
        }
        return total;
    }
    
    @Override
    public double getRemovePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getRemovePerSecond();
            }
        }
        return total;
    }
    
    @Override
    public long getChangeCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getChangeCount();
            }
        }
        return total;
    }

    @Override
    public double getChangePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getChangePerSecond();
            }
        }
        return total;
    }    
    
    @Override
    public long getObjectCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getObjectCount();
            }
        }
        return total;
    }

    @Override
    public long getNotifyTemplateCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getNotifyTemplateCount();
            }
        }
        return total;
    }

    @Override
    public long getActiveConnectionCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getActiveConnectionCount();
            }
        }
        return total;
    }

    @Override
    public long getActiveTransactionCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getActiveTransactionCount();
            }
        }
        return total;
    }
}
