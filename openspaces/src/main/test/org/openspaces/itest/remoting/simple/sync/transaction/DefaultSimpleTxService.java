package org.openspaces.itest.remoting.simple.sync.transaction;


import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.RemotingService;

/**
 * @author kimchy
 */
@RemotingService
public class DefaultSimpleTxService implements SimpleTxService {

    private GigaSpace gigaSpace;

    private boolean transactional;

    public void bahh(TestMessage testMessage) {
        transactional = gigaSpace.getCurrentTransaction() != null;
        gigaSpace.write(testMessage);
        if (testMessage.getMessage().equals("throwme")) {
            throw new RuntimeException();
        }
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public boolean isTransactional() {
        return transactional;
    }
}