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
package org.openspaces.grid.gsm.machines.plugins;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.gigaspaces.internal.utils.concurrent.GSThreadFactory;
import com.j_spaces.kernel.SystemProperties;
import com.j_spaces.kernel.threadpool.DynamicExecutors;

public class NonBlockingElasticMachineProvisioningAdapterFactory {

	private static final int MIN_THREADS = 1;
	private static final int MAX_THREADS = Integer.getInteger(SystemProperties.ESM_MACHINE_PROVISIONING_MAX_THREADS, 64);
	private static final long KEEP_ALIVE_TIME = TimeUnit.SECONDS.toMillis(60);

	private final ThreadFactory threadFactory = new GSThreadFactory(this.getClass().getName(), /*daemonThreads=*/ true);
    private ExecutorService service = DynamicExecutors.newScalingThreadPool(MIN_THREADS, MAX_THREADS, KEEP_ALIVE_TIME, threadFactory);
   
    private ScheduledThreadPoolExecutor scheduledExecutorService = 
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, threadFactory);
    
    public NonBlockingElasticMachineProvisioningAdapter create(ElasticMachineProvisioning machineProvisioning) {
        return new NonBlockingElasticMachineProvisioningAdapter(machineProvisioning, service, scheduledExecutorService);
    }
    
    public void destroy() {
        service.shutdownNow();
        scheduledExecutorService.shutdownNow();
    }
}
