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
 * A data counter that periodically performs a count on the space and updates
 * its count of data objects.
 *
 * <p>Note, since we simply use the GigaSpace API, with a "null" Data object
 * template, we simply count how many data objects are in the space. In our
 * example, we show how a Local View can be used to hold all the processed
 * data objects, and the count is executed on it. The Local View expoess the
 * same API as the Space.
 *
 * <p>Also note, this of course can be implemented in many different ways,
 * one of them is using notifications (using the notify container), and another
 * would be to execute the count against the actual space (without a view).
 * This bean is here to show how LocalView can be used.
 *
 * @author kimchy
 */
public class ViewDataCounter implements InitializingBean, DisposableBean {

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    private ViewCounterTask viewCounterTask;

    private long defaultDelay = 1000;

    @GigaSpaceContext(name = "processedViewGigaSpace")
    private GigaSpace gigaSpace;

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING VIEW COUNTER WITH CYCLE [" + defaultDelay + "]");
        viewCounterTask = new ViewCounterTask();
        executorService = Executors.newScheduledThreadPool(1);
        sf = executorService.scheduleAtFixedRate(viewCounterTask, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
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

        public int getLatestCount() {
            return latestCount;
        }
    }

    public int getProcessedDataCount() {
        return viewCounterTask.getLatestCount();
    }
}
