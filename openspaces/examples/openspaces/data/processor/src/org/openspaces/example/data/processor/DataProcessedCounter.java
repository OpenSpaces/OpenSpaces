package org.openspaces.example.data.processor;

import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.example.data.common.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple bean counting the number of processed data. Holds a simple
 * counter that is incremented each time a matching event occurs.
 *
 * <p>Note, though the name indicates counting processed data events, it
 * is a matter of configuration what this bean will count. In our case, the
 * template will match all the Data objects that has the processed flag set
 * to true.
 *
 * <p>Also note, the processed data that will be counted depends on the
 * configuration. For example, this example uses the "non clustered" view
 * of the space while running within an embedded space. This means this
 * coutner will count only the relevant partition processed data. It is
 * just a matter of configuration to count the number of processed data
 * across a cluster.
 *
 * @author kimchy
 */
public class DataProcessedCounter {

    AtomicInteger processedDataCount = new AtomicInteger(0);

    @SpaceDataEvent
    public void dataProcessed(Data data) {
        processedDataCount.incrementAndGet();
        System.out.println("*** PROCESSED DATA COUNT [" + processedDataCount + "] DATA [" + data + "]");
    }

    public int getProcessedDataCount() {
        return processedDataCount.intValue();
    }
}
