package org.openspaces.admin.pu.elastic.events;

public interface ElasticProcessingUnitFailureEvent extends ElasticProcessingUnitEvent {

    /**
     * @return the list of processing units that may be affected by this failure to start a new machine
     */
    String[] getProcessingUnitNames();

    /**
     * @return the failure description
     */
    String getFailureDescription();
}
