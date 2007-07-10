/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.example.data.processor;

import org.openspaces.example.data.common.Data;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

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

    public void dataProcessed(Data data) {
        processedDataCount.incrementAndGet();
        System.out.println("*** PROCESSED DATA COUNT [" + processedDataCount + "] DATA [" + data + "]");
    }

    public int getProcessedDataCount() {
        return processedDataCount.intValue();
    }
}
