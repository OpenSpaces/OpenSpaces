package org.openspaces.example.data.feeder;

import org.openspaces.example.data.common.Data;
import org.openspaces.example.data.common.IDataProcessor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A bean that uses OpenSpaces Remoting support to invoke IDataProcessor implementation
 * exposed by another processing unit.
 *
 * <p>Stats up a scheduled taks that invokes both IDataProcessor APIs periodically. Uses
 * java.util.concurrent Scheduled Executor Service.
 *
 * <p>Note, this bean simply uses IDataProcessor, OpenSpaces Remoting hides the fact that
 * this interface will actually cause a remote invocation, with the Space as the transport
 * layer, directed into a serivce exposed by another processing unit.
 *
 * @author kimchy
 */
public class DataRemoting implements InitializingBean, DisposableBean {

    private long defaultDelay = 1000;

    private IDataProcessor dataProcessor;

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    public void setDataProcessor(IDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING REMOTING WITH CYCLE [" + defaultDelay + "]");
        executorService = Executors.newScheduledThreadPool(1);
        sf = executorService.scheduleAtFixedRate(new DataFeederTask(), defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
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
                Data data = new Data(Data.TYPES[counter++ % Data.TYPES.length], "REMOTING " + Long.toString(time));
                data.setProcessed(false);
                System.out.println("--- REMOTING PARAMTER " + data);
                dataProcessor.sayData(data);
                data = dataProcessor.processData(data);
                System.out.println("--- REMOTING RESULT   " + data);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}