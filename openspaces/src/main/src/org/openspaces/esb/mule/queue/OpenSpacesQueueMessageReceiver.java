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

package org.openspaces.esb.mule.queue;

import org.mule.impl.MuleMessage;
import org.mule.providers.PollingReceiverWorker;
import org.mule.providers.TransactedPollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.CreateException;
import org.mule.umo.provider.UMOConnector;

import java.util.LinkedList;
import java.util.List;

/**
 * @author kimchy
 */
public class OpenSpacesQueueMessageReceiver extends TransactedPollingMessageReceiver {

    private OpenSpacesQueueConnector connector;


    private Object template;

    public OpenSpacesQueueMessageReceiver(UMOConnector connector, UMOComponent component, UMOImmutableEndpoint endpoint) throws CreateException {
        super(connector, component, endpoint);
        this.setReceiveMessagesInTransaction(endpoint.getTransactionConfig().isTransacted());
        this.connector = (OpenSpacesQueueConnector) connector;
    }

    protected void doConnect() throws Exception {
        InternalQueueEntry internalTemplate = new InternalQueueEntry();
        internalTemplate.endpointURI = endpoint.getEndpointURI().getAddress();
        template = connector.getGigaSpace().snapshot(internalTemplate);
    }

    protected void doDispose() {
        // template method
    }

    protected void doDisconnect() throws Exception {
        // template method
    }

    public Object onCall(UMOMessage message, boolean synchronous) throws UMOException {
        // Rewrite the message to treat it as a new message
        UMOMessage newMessage = new MuleMessage(message.getPayload(), message);
        return routeMessage(newMessage, synchronous);
    }

    protected List getMessages() throws Exception {
        // The list of retrieved messages that will be returned
        List<UMOMessage> messages = new LinkedList<UMOMessage>();

        /*
         * Determine how many messages to batch in this poll: we need to drain the queue quickly, but not by
         * slamming the workManager too hard. It is impossible to determine this more precisely without proper
         * load statistics/feedback or some kind of "event cost estimate". Therefore we just try to use half
         * of the receiver's workManager, since it is shared with receivers for other endpoints.
         */
        int maxThreads = connector.getReceiverThreadingProfile().getMaxThreadsActive();
        // also make sure batchSize is always at least 1
        int batchSize = Math.max(1, Math.min(connector.getGigaSpace().count(template), ((maxThreads / 2) - 1)));

        // try to get the first event off the queue
        InternalQueueEntry entry = (InternalQueueEntry) connector.getGigaSpace().take(template, connector.getTimeout());

        if (entry.message != null) {
            // keep first dequeued event
            messages.add(entry.message);

            // keep batching if more events are available
            for (int i = 0; i < batchSize && entry != null; i++) {
                entry = (InternalQueueEntry) connector.getGigaSpace().take(template, 0);
                if (entry != null) {
                    messages.add(entry.message);
                }
            }
        }

        // let our workManager handle the batch of events
        return messages;
    }

    protected void processMessage(Object msg) throws Exception {
        // getMessages() returns UMOEvents
        UMOMessage message = (UMOMessage) msg;

        // Rewrite the message to treat it as a new message
        UMOMessage newMessage = new MuleMessage(message.getPayload(), message);
        routeMessage(newMessage);
    }

    /*
     * We create our own "polling" worker here since we need to evade the standard scheduler.
     */
    // @Override
    protected PollingReceiverWorker createWork() {
        return new ReceiverWorker(this);
    }

    /*
     * Even though the OpenSpaces Queue transport is "polling" for messages, the nonexistent cost of accessing the queue is
     * a good reason to not use the regular scheduling mechanism in order to both minimize latency and
     * maximize throughput.
     */
    protected static class ReceiverWorker extends PollingReceiverWorker {

        public ReceiverWorker(OpenSpacesQueueMessageReceiver pollingMessageReceiver) {
            super(pollingMessageReceiver);
        }

        public void run() {
            /*
             * We simply run our own polling loop all the time as long as the receiver is started. The
             * blocking wait defined by VMConnector.getQueueTimeout() will prevent this worker's receiver
             * thread from busy-waiting.
             */
            while (this.getReceiver().isConnected()) {
                super.run();
            }
        }
    }

}
