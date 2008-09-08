/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.example.data.feeder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openspaces.core.SpaceInterruptedException;
import org.openspaces.example.data.common.IDataProcessor;
import org.openspaces.example.data.feeder.support.BroadcastCounterReducer;
import org.openspaces.remoting.SyncProxy;
import org.springframework.util.Assert;

/**
 * A data counter that periodically performs a count on the space and updates
 * its count of data objects.
 *
 * <p>The counter uses {@link org.openspaces.example.data.common.IDataProcessor}
 * which is proxied using executor remoting (with broadcast enabled) which will cause
 * counting of all the processed data in the Space.
 *
 * @author kimchy
 */
public class BroadcastDataCounter {

    @SyncProxy(gigaSpace = "gigaSpace", remoteResultReducerType = BroadcastCounterReducer.class, broadcast = true)
    private IDataProcessor dataProcessor;

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    private ViewCounterTask viewCounterTask;

    private long defaultDelay = 1000;

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    @PostConstruct
    public void construct() throws Exception {
        Assert.notNull(dataProcessor, "dataProcessor proeprty must be set");
        System.out.println("--- STARTING BROADCAST REMOTING COUNTER WITH CYCLE [" + defaultDelay + "]");
        viewCounterTask = new ViewCounterTask();
        executorService = Executors.newScheduledThreadPool(1);
        sf = executorService.scheduleAtFixedRate(viewCounterTask, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        sf.cancel(true);
        sf = null;
        executorService.shutdown();
    }

    public class ViewCounterTask implements Runnable {

        private long latestCount = -1;

        public void run() {
            try {
                long count = dataProcessor.countDataProcessed();
                if (latestCount != count) {
                    System.out.println("**** BROADCAST REMOTING COUNT IS [" + count + "]");
                    latestCount = count;
                }
            } catch (SpaceInterruptedException e) {
                // ignore, we are shutting down (being interrupted)
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public long getLatestCount() {
            return latestCount;
        }
    }

    public long getProcessedDataCount() {
        return viewCounterTask.getLatestCount();
    }
}