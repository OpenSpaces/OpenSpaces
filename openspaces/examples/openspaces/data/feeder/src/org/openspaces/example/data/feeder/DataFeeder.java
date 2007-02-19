package org.openspaces.example.data.feeder;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.example.data.common.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DataFeeder implements InitializingBean, DisposableBean {

    private ScheduledExecutorService executorService;

    private ScheduledFuture sf;

    private long defaultDelay = 1000;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void afterPropertiesSet() throws Exception {
        executorService = Executors.newScheduledThreadPool(1);
        sf = executorService.scheduleAtFixedRate(new DataFeederTask(), defaultDelay, defaultDelay, TimeUnit.MILLISECONDS);
    }


    public void destroy() throws Exception {
        sf.cancel(true);
        sf = null;
        executorService.shutdown();
    }

    public class DataFeederTask implements Runnable {

        private int counter;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                Data data = new Data(Data.TYPES[counter++ % Data.TYPES.length], "FEEDER " + Long.toString(time));
                gigaSpace.write(data);
                System.out.println("WROTE " + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
