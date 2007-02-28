package org.openspaces.example.data.processor;

import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.example.data.common.Data;

/**
 * @author kimchy
 */
public class DataProcessedCounter {

    int processedDataCount = 0;

    @SpaceDataEvent
    public void dataProcessed(Data data) {
        processedDataCount++;
        System.out.println("*** PROCESSED DATA COUNT [" + processedDataCount + "] DATA [" + data + "]");
    }
}
