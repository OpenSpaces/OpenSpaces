package org.openspaces.grid.gsm.machines.plugins;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.j_spaces.kernel.GSThreadFactory;

public class NonBlockingElasticMachineProvisioningAdapterFactory {

    private ExecutorService service = Executors
    .newCachedThreadPool(new GSThreadFactory(this.getClass().getName(),true));
   
    private ScheduledThreadPoolExecutor scheduledExecutorService = 
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
                1,
                new GSThreadFactory(this.getClass().getName(),true));
    
    public NonBlockingElasticMachineProvisioningAdapter create(ElasticMachineProvisioning machineProvisioning) {
        return new NonBlockingElasticMachineProvisioningAdapter(machineProvisioning, service, scheduledExecutorService);
    }
    
    public void destroy() {
        service.shutdownNow();
        scheduledExecutorService.shutdownNow();
    }
}
