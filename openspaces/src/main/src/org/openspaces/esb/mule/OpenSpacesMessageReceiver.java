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

package org.openspaces.esb.mule;

import org.mule.MuleRuntimeException;
import org.mule.config.i18n.CoreMessages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.AbstractReceiverWorker;
import org.mule.umo.TransactionException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.CreateException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.AbstractEventListenerContainer;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.Properties;

/**
 * <code>OpenSpacesMessageReceiver</code> is used to receive data from an GigaSpaces's space.
 * It implements SpaceDataEventListener and as such it register itself to Polling/Notify SpaceListeningContainer.
 *
 * @author yitzhaki
 * @see org.openspaces.events.SpaceDataEventListener
 * @see org.mule.umo.provider.UMOMessageReceiver
 */
public class OpenSpacesMessageReceiver extends AbstractMessageReceiver implements SpaceDataEventListener {

    private static final String ENDPOINT_PARAM_WORK_MANAGER = "workManager";

    private AbstractEventListenerContainer eventListenerContainer;

    private boolean workManager = false;


    /**
     * Creates a OpenSpacesMessageReceiver and resister it as a SpaceDataEventListener to
     * the Polling/Notify container that declared as umoEndpoint.EndpointURI.address.
     *
     * @param connector the endpoint that created this listener
     * @param component the component to associate with the receiver. When data is
     *                  received the component <code>dispatchEvent</code> or
     *                  <code>sendEvent</code> is used to dispatch the data to the
     *                  relivant UMO.
     * @param endpoint  the provider contains the endpointUri on which the receiver
     *                  will listen on. The URI structure must be declared as the following
     *                  os://<Polling/Notify container id>
     * @see UMOComponent
     * @see UMOEndpoint
     */
    public OpenSpacesMessageReceiver(UMOConnector connector, UMOComponent component, UMOImmutableEndpoint endpoint) throws CreateException {
        super(connector, component, endpoint);
        ApplicationContext applicationContext = ((OpenSpacesConnector) connector).getApplicationContext();
        if (applicationContext == null) {
            throw new CreateException(CoreMessages.connectorWithProtocolNotRegistered(connector.getProtocol()), this);
        }

        initWritingAttributes(endpoint);
        String eventListenerContainerName = endpoint.getEndpointURI().getAddress();
        eventListenerContainer = (AbstractEventListenerContainer) applicationContext.getBean(eventListenerContainerName);
        eventListenerContainer.setEventListener(this);
        eventListenerContainer.start();

    }

    /**
     * Extract the workManager setting from the URI. If the atrribute is missing sets
     * it to the default (<code>false</code>).
     */
    private void initWritingAttributes(UMOImmutableEndpoint endpoint) {
        Properties params = endpoint.getEndpointURI().getParams();
        if (params != null) {
            try {
                String workManager = (String) params.get(ENDPOINT_PARAM_WORK_MANAGER);
                if (workManager != null) {
                    this.workManager = Boolean.valueOf(workManager);
                }
            } catch (Exception e) {
                throw new MuleRuntimeException(CoreMessages.failedToCreateConnectorFromUri(endpoint.getEndpointURI()), e);
            }
        }
    }

    /**
     * An event callback with the actual data object of the event.
     * This method invoked by eventListenerContainer creates a UMOMessage that wraps the data object,
     * then routes the UMOMessage via the Mule bus.
     *
     * @param data      The actual data object of the event
     * @param gigaSpace A GigaSpace instance that can be used to perofrm additional operations against the
     *                  space
     * @param txStatus  An optional transaction status allowing to rollback a transaction programmatically
     * @param source    Optional additional data or the actual source event data object (where relevant)
     */
    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        try {
            if (workManager) {
                getWorkManager().scheduleWork(new GigaSpaceWorker(data, this));
            } else {
                UMOMessageAdapter adapter = connector.getMessageAdapter(data);
                UMOMessage message = new MuleMessage(adapter);
                // nothing to do with the result
                routeMessage(message);
            }
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
            //todo
//            handleException(e);
        }
    }


    protected class GigaSpaceWorker extends AbstractReceiverWorker {

        public GigaSpaceWorker(Object message, AbstractMessageReceiver receiver) {
            super(new ArrayList(1), receiver);
            messages.add(message);
        }

        protected void bindTransaction(UMOTransaction tx) throws TransactionException {
            //todo:support transaction
        }
    }

    protected void doStart() throws UMOException {
        eventListenerContainer.start();
    }

    protected void doDisconnect() throws Exception {
        eventListenerContainer.setEventListener(null);
        eventListenerContainer.stop();
        //todo: this is a patch find better solution.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void doStop() throws UMOException {
    }

    protected void doConnect() throws Exception {
    }

    protected void doDispose() {
    }
}
