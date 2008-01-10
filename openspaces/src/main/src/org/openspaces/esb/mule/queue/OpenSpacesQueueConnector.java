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

import org.mule.impl.endpoint.DynamicEndpointURIEndpoint;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.AbstractConnector;
import org.mule.routing.filters.WildcardFilter;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.EndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Iterator;

/**
 * @author kimchy
 */
public class OpenSpacesQueueConnector extends AbstractConnector implements ApplicationContextAware {

    public static final String OS_QUEUE = "os-queue";


    private String gigaSpaceRef;

    private boolean fifo = false;

    private boolean persistent = false;

    private long timeout = 1000;


    private ApplicationContext applicationContext;

    private GigaSpace gigaSpace;


    /**
     * @return the openspaces protocol name.
     */
    public String getProtocol() {
        return OS_QUEUE;
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setGigaSpace(String gigaSpaceRef) {
        this.gigaSpaceRef = gigaSpaceRef;
    }

    public String getGigaSpace() {
        return gigaSpaceRef;
    }

    public boolean isFifo() {
        return fifo;
    }

    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    protected void doInitialise() throws InitialisationException {
    }

    protected void doDispose() {
    }

    protected void doStart() throws UMOException {
    }

    protected void doStop() throws UMOException {
    }

    protected void doConnect() throws Exception {
        if (gigaSpaceRef == null) {
            String[] beansNames = applicationContext.getBeanNamesForType(GigaSpace.class);
            if (beansNames != null && beansNames.length == 1) {
                gigaSpace = (GigaSpace) applicationContext.getBean(beansNames[0]);
            } else {
                throw new RuntimeException("No GigaSpace ref is configured, and more than one GigaSpace bean is configured");
            }
        } else {
            gigaSpace = (GigaSpace) applicationContext.getBean(gigaSpaceRef);
        }
    }

    protected void doDisconnect() throws Exception {
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public GigaSpace getGigaSpaceObj() {
        return gigaSpace;
    }

    OpenSpacesQueueMessageReceiver getReceiver(UMOEndpointURI endpointUri) throws EndpointException {
        return (OpenSpacesQueueMessageReceiver) getReceiverByEndpoint(endpointUri);
    }

    protected UMOMessageReceiver getReceiverByEndpoint(UMOEndpointURI endpointUri) throws EndpointException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up vm receiver for address: " + endpointUri.toString());
        }

        UMOMessageReceiver receiver;
        // If we have an exact match, use it
        receiver = (UMOMessageReceiver) receivers.get(endpointUri.getAddress());
        if (receiver != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found exact receiver match on endpointUri: " + endpointUri);
            }
            return receiver;
        }

        // otherwise check each one against a wildcard match
        for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();) {
            receiver = (UMOMessageReceiver) iterator.next();
            String filterAddress = receiver.getEndpointURI().getAddress();
            WildcardFilter filter = new WildcardFilter(filterAddress);
            if (filter.accept(endpointUri.getAddress())) {
                UMOImmutableEndpoint endpoint = receiver.getEndpoint();
                UMOEndpointURI newEndpointURI = new MuleEndpointURI(endpointUri, filterAddress);
                receiver.setEndpoint(new DynamicEndpointURIEndpoint(endpoint, newEndpointURI));

                if (logger.isDebugEnabled()) {
                    logger.debug("Found receiver match on endpointUri: " + receiver.getEndpointURI()
                            + " against " + endpointUri);
                }
                return receiver;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("No receiver found for endpointUri: " + endpointUri);
        }
        return null;
    }
}
