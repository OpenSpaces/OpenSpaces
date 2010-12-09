package org.openspaces.wan.mirror;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.lease.Lease;

import org.openspaces.core.GigaSpace;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.UpdateModifiers;

public class LogCleanupRunnable implements Runnable {

    private static final Logger logger = Logger.getLogger(LogCleanupRunnable.class.getName());
    public LogCleanupRunnable(GigaSpace wanGigaSpace, TransactionTemplate wanTransactionTemplate, int numOfLocations,
            SQLQuery<WanEntry> deleteLogQuery) {
    
        this.wanGigaSpace = wanGigaSpace;
        this.wanTransactionTemplate = wanTransactionTemplate;
        this.numOfLocations = numOfLocations;
        this.deleteLogQuery = deleteLogQuery;
    }


    private GigaSpace wanGigaSpace;
    private TransactionTemplate wanTransactionTemplate;    
    private int numOfLocations;
    private SQLQuery<WanEntry> deleteLogQuery;
    
    public void run() {
        this.wanTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                runLogCleanUpTask(status);
            }
        });

        
    }
    
    private boolean isTimeForCleanUp() {
        final LogCleanupTimestamp template = new LogCleanupTimestamp(0);
        final LogCleanupTimestamp timeStamp = wanGigaSpace.read(template, ReadModifiers.EXCLUSIVE_READ_LOCK);
        if (timeStamp == null) { // first time) {
            return true;
        }
        final long now = System.currentTimeMillis();
        return (now - timeStamp.getTimeStamp() > 30 * 1000);

    }

    
    private void runLogCleanUpTask(final TransactionStatus status) {

        try {
            // first get the log cleanup time stamp
            if (!isTimeForCleanUp()) {
                return;
            }

            // first collect all Read indices
            final ReadIndex template = new ReadIndex(0, 0, 0, 0);
            final int numOfReadIndices = numOfLocations * (numOfLocations - 1);

            final ReadIndex[] readIndices = this.wanGigaSpace.readMultiple(template, numOfReadIndices);

            final Map<Integer, Long> minValBySiteId = new HashMap<Integer, Long>();
            for (int i=1;i<= this.numOfLocations;++i) {
                minValBySiteId.put(i, Long.MAX_VALUE);
            }

            for (final ReadIndex readIndex : readIndices) {
                final int site = readIndex.getTargetSiteId();
                final long val = readIndex.getLogIndex();
                final long oldVal = minValBySiteId.get(site);
                minValBySiteId.put(site, Math.min(oldVal, val));
            }

            int paramIndex = 1;
            for (final Map.Entry<Integer, Long> entry : minValBySiteId.entrySet()) {
                this.deleteLogQuery.setParameter(paramIndex, entry.getKey());
                ++paramIndex;
                // NOTE: to avoid any issues with transactionality, it is better
                // to
                // delete
                // old logs up to the last one, just in case we get some sort of
                // end
                // case update
                // where another transaction is still using these entries.
                // This is not supposed to occur, but better safe then sorry.
                // The entries we leave behind will be deleted on the next
                // iteration
                // of the
                // log clean up task
                this.deleteLogQuery.setParameter(paramIndex, entry.getValue() - 2);
                ++paramIndex;
            }

            final WanEntry[] wanEntries = wanGigaSpace.takeMultiple(this.deleteLogQuery, Integer.MAX_VALUE);
            logger.info("Removed " + wanEntries.length + " elements from the Log Table");

            final LogCleanupTimestamp cleanUpCompletion = new LogCleanupTimestamp(System.currentTimeMillis());
            wanGigaSpace.write(cleanUpCompletion, Lease.FOREVER, WanDataSource.WAN_SPACE_ACCESS_TIMEOUT,
                    UpdateModifiers.UPDATE_OR_WRITE);

        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Log clean up task failed! Problem was: " + e.getMessage(), e);
            status.setRollbackOnly();

        }

    }

}
