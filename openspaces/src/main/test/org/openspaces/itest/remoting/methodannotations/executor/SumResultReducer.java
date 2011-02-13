package org.openspaces.itest.remoting.methodannotations.executor;

import org.openspaces.remoting.RemoteResultReducer;
import org.openspaces.remoting.SpaceRemotingInvocation;
import org.openspaces.remoting.SpaceRemotingResult;

/**
 * @author uri
 */
public class SumResultReducer implements RemoteResultReducer<Integer, Integer>{
    public Integer reduce(SpaceRemotingResult<Integer>[] spaceRemotingResults, SpaceRemotingInvocation remotingInvocation) throws Exception {
        int sum = 0;
        for (SpaceRemotingResult<Integer> spaceRemotingResult : spaceRemotingResults) {
            Throwable exception = spaceRemotingResult.getException();
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            Integer result = spaceRemotingResult.getResult();
            if (result != null) {
                sum += result;
            }
        }
        return sum;
    }
}
