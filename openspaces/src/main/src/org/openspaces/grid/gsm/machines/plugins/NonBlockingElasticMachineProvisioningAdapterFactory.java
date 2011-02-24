package org.openspaces.grid.gsm.machines.plugins;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.j_spaces.kernel.GSThread;

public class NonBlockingElasticMachineProvisioningAdapterFactory {

    private ExecutorService service = Executors
    .newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new GSThread(r, this.getClass().getName());
        }

    });
   
    private ScheduledThreadPoolExecutor scheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1,
            new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new GSThread(r, this.getClass().getName());
            }});
    
    public NonBlockingElasticMachineProvisioningAdapter create(ElasticMachineProvisioning machineProvisioning) {
        return new NonBlockingElasticMachineProvisioningAdapter(machineProvisioning, service, scheduledExecutorService);
    }
    
    public void destroy() {
        service.shutdownNow();
        scheduledExecutorService.shutdownNow();
    }
}
