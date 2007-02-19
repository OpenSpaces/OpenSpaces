package org.openspaces.example.data.processor;

import org.openspaces.events.adapter.SpaceDataEvent;

/**
 * @author kimchy
 */
public class DataProcessedCounter {

    int processedDataCount = 0;

    @SpaceDataEvent
    public void dataProcessed() {
        processedDataCount++;
        System.out.println("*** PROCESSED DATA COUNT [" + processedDataCount + "]");
    }
}
