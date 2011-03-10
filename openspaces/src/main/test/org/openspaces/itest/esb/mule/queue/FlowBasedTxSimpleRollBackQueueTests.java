package org.openspaces.itest.esb.mule.queue;

public class FlowBasedTxSimpleRollBackQueueTests extends TxXASimpleRollBackQueueTests {

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/queue/flow-tx-xa-simple-rollback.xml";
    }
}
