package ${puGroupId}.feeder;

import ${puGroupId}.common.Data;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceInterruptedException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * A feeder bean starts a scheduled task that writes a new Data objects to the space
 * (in an unprocessed state).
 *
 * <p>The scheduling uses the java.util.concurrent Scheduled Executor Service. It
 * is started and stopped based on Spring life cycle events.
 *
 * @author kimchy
 */
public class Feeder implements InitializingBean, DisposableBean {

    private long numberOfTypes = 10;

    private long defaultDelay = 1000;

    private FeederTask feederTask;

    private GigaSpace gigaSpace;

    private Thread feederThread;
    
    /**
     * Sets the number of types that will be used to set {@link org.openspaces.example.data.common.Data#setType(Long)}.
     *
     * <p>The type is used as the routing index for partitioned space. This will affect the distribution of Data
     * objects over a partitioned space.
     */
    public void setNumberOfTypes(long numberOfTypes) {
        this.numberOfTypes = numberOfTypes;
    }

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }
    
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }


    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING FEEDER WITH CYCLE [" + defaultDelay + "]");
        feederTask = new FeederTask();
        feederThread = new Thread(feederTask);
        feederThread.start();
    }

    public void destroy() throws Exception {
        feederTask.cancel();
    }
    
    public long getFeedCount() {
        return feederTask.getCounter();
    }

    
    public class FeederTask implements Runnable {

        private long counter = 1;
        private volatile boolean alive = true;
    
        public void run() {
            try {
                while (alive) {
                    long time = System.currentTimeMillis();
                    Data data = new Data(new Long(counter++ % numberOfTypes), "FEEDER " + Long.toString(time));
                    gigaSpace.write(data);
                    System.out.println("--- FEEDER WROTE " + data);
                    Thread.sleep(defaultDelay);
                }
            } catch (SpaceInterruptedException e) {
                // ignore, we are being shutdown
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void cancel() {
            alive = false;
        }

        public long getCounter() {
            return counter;
        }
    }

}
