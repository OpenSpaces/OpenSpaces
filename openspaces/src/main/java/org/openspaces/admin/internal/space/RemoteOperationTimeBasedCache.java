package org.openspaces.admin.internal.space;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.DefaultAdmin;

import com.j_spaces.kernel.time.SystemTime;

public abstract class RemoteOperationTimeBasedCache<T> {

    private static final Log logger = LogFactory.getLog(DefaultAdmin.class);

	private final Object monitor = new Object();
    private final long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;
    private long lastStatisticsTimestamp = 0;
    private volatile T lastInstance;

    public T get() {
        synchronized (monitor){

            long currentTime = SystemTime.timeMillis();
            if( ( currentTime - lastStatisticsTimestamp ) < statisticsInterval ) {
                return lastInstance;
            }

            lastStatisticsTimestamp = currentTime;
            try {
                this.lastInstance = load();
            } 
            catch( RemoteException e ) {

            	logger.debug("RemoteException caught while trying to retrieve Space Runtime Info from space admin.", e );
                return null;
            }

            return this.lastInstance;
        }
    }
    
    protected abstract T load() throws RemoteException;
}
