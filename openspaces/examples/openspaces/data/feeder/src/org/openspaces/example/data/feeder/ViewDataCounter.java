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
 * @author kimchy
 */
public class ViewDataCounter implements InitializingBean, DisposableBean {

    private ScheduledExecutorService executorService;

    private ScheduledFuture sf;

    private long defaultDelay = 1000;

    @GigaSpaceContext(name = "processedViewGigaSpace")
    private GigaSpace gigaSpace;

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING VIEW COUNTER WITH CYCLE [" + defaultDelay + "]");
        executorService = Executors.newScheduledThreadPool(1);
        sf = executorService.scheduleAtFixedRate(new ViewCounterTask(), defaultDelay, defaultDelay, TimeUnit.MILLISECONDS);
    }


    public void destroy() throws Exception {
        sf.cancel(true);
        sf = null;
        executorService.shutdown();
    }

    public class ViewCounterTask implements Runnable {

        private int latestCount = -1;

        public void run() {
            try {
                int count = gigaSpace.count(new Data());
                if (latestCount != count) {
                    System.out.println("---- VIEW COUNT IS [" + count + "]");
                    latestCount = count;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
