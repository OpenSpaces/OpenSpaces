package org.openspaces.itest.remoting.simple.transactionalservice;


import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.RemotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author kimchy
 */
@RemotingService
@Transactional
public class DefaultSimpleTxService implements SimpleTxService {

    @Autowired
    private GigaSpace gigaSpace;

    public void bahh(TestMessage testMessage) {
        gigaSpace.write(testMessage);
        if (testMessage.getMessage().equals("throwme")) {
            throw new RuntimeException();
        }
    }
}