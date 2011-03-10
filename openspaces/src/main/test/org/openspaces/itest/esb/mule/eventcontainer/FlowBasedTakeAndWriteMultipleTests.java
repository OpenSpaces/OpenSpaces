package org.openspaces.itest.esb.mule.eventcontainer;

public class FlowBasedTakeAndWriteMultipleTests extends TakeAndWriteMultipleTests{

    @Override
    protected String getConfigResources() {
        return "org/openspaces/itest/esb/mule/eventcontainer/flow-takeandwritemultiple.xml";
    }
}
