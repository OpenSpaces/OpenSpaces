package org.openspaces.example.data.feeder.support;

import org.openspaces.remoting.RemoteResultReducer;
import org.openspaces.remoting.SpaceRemotingInvocation;
import org.openspaces.remoting.SpaceRemotingResult;

/**
 * A sync remoting reducer (when used in broadcast mode) iterating through the
 * results of {@link org.openspaces.example.data.common.IDataProcessor#countDataProcessed()}
 * performed on all the cluster nodes and reducing the result by aggregating it.
 *
 * @author kimchy
 */
public class SyncBroadcastCounterReducer implements RemoteResultReducer {

    public Object reduce(SpaceRemotingResult[] results, SpaceRemotingInvocation remotingInvocation) throws Exception {
        if (!remotingInvocation.getMethodName().equals("countDataProcessed")) {
            return results[0].getResult();
        }
        long totalCount = 0;
        for (SpaceRemotingResult result : results) {
            if (result.getException() != null) {
                // just log the fact that there was an exception
                System.out.println("SYNC REMOTING COUNT EXCEPTION " + result.getException().getMessage());
                continue;
            }
            totalCount += (Long) result.getResult();
        }
        return totalCount;
    }
}
