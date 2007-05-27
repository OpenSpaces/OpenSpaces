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

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.example.data.common.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A feeder bean started a scheduled task that writes a new Data object to the space.
 *
 * <p>The space is injected into this bean using OpenSpaces support for @GigaSpaceContext
 * annoation.
 *
 * <p>The scheduled support uses the java.util.concurrent Scheduled Executor Service. It
 * is started and stopped based on Spring lifeceycle events.
 *
 * @author kimchy
 */
public class DataFeeder implements InitializingBean, DisposableBean {

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    private long defaultDelay = 1000;

    private DataFeederTask dataFeederTask;

    private Long instanceId;

    private long startIdFrom = 0;

    @GigaSpaceContext(name = "gigaSpace")
    private GigaSpace gigaSpace;

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING FEEDER WITH CYCLE [" + defaultDelay + "]");
        if (instanceId != null) {
            // have a range of ids based on the instance id of the procesing unit
            startIdFrom = instanceId * 10000000;
        }
        executorService = Executors.newScheduledThreadPool(1);
        dataFeederTask = new DataFeederTask();
        sf = executorService.scheduleAtFixedRate(dataFeederTask, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
    }

    public void destroy() throws Exception {
        sf.cancel(true);
        sf = null;
        executorService.shutdown();
    }

    public class DataFeederTask implements Runnable {

        private long counter;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                Data data = new Data(Data.TYPES[(int) (counter++ % Data.TYPES.length)], "FEEDER " + Long.toString(time));
                data.setId(startIdFrom + counter);
                data.setProcessed(false);
                gigaSpace.write(data);
                System.out.println("--- WROTE " + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public long getCounter() {
            return counter;
        }
    }

    public long getFeedCount() {
        return dataFeederTask.getCounter();
    }
}
