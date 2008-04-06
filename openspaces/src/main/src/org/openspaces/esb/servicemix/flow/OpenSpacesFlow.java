/*
* Copyright 2006-2007 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openspaces.esb.servicemix.flow;

import com.gigaspaces.events.DataEventSession;
import com.gigaspaces.events.EventSessionConfig;
import com.gigaspaces.events.EventSessionFactory;
import com.gigaspaces.events.NotifyActionType;
import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import com.j_spaces.core.client.MetaDataEntry;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.transaction.TransactionException;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.executors.Executor;
import org.apache.servicemix.jbi.event.ComponentAdapter;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.event.EndpointAdapter;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.EndpointListener;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceInterruptedException;
import org.springframework.dao.DataAccessException;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implmention of {@link org.apache.servicemix.jbi.nmr.flow.Flow} based on gigaSpaces.
 * This implmention uses the Space as the traffic backbone.
 * Use for message routing among a network of containers.
 *
 * @author yitzhaki
 * @org.apache.xbean.XBean element="openspacesFlow"
 */
public class OpenSpacesFlow extends AbstractFlow {

    private GigaSpace gigaSpace;

    private AtomicBoolean started = new AtomicBoolean(false);

    private EndpointListener endpointListener;

    private ComponentListener componentListener;

    private Executor executor;

    private DataEventSession endpointRegistrySession;

    private EventRegistration endpointRegistryRegistration;

    private Map<String, MessageConsumer> consumerMap = new ConcurrentHashMap<String, MessageConsumer>();

    private boolean fifo = false;

    private boolean persistent = false;

    private long timeout = 1000;


    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * The type of Flow
     *
     * @return the type
     */
    public String getDescription() {
        return "openspaces";
    }


    /**
     * Check if the flow can support the requested QoS for this exchange
     *
     * @param me the exchange to check
     * @return true if this flow can handle the given exchange
     */
    public boolean canHandle(MessageExchange me) {
        if (isTransacted(me)) {
            return false;
        }
        if (isPersistent(me)) {
            return isPersistent();
        }
        return true;
    }

    /**
     * Initialize the Region
     *
     * @param broker Broker
     * @throws JBIException
     */
    public void init(final Broker broker) throws JBIException {
        log.debug(broker.getContainer().getName() + ": Initializing openspaces flow");
        super.init(broker);
        try {
            //Find executor
            executor = broker.getContainer().getExecutorFactory().createExecutor("flow.openspaces");

        } catch (Exception e) {
            log.error("Failed to initialize OpenSpacesFlow", e);
            throw new JBIException(e);
        }
    }


    /**
     * Start the flow.
     *
     * @throws JBIException
     */
    public void start() throws JBIException {
        if (started.compareAndSet(false, true)) {
            log.debug(broker.getContainer().getName() + ": Starting openspaces flow");
            super.start();

            try {
                addEndpointListener(broker);
                addComponentListener(broker);
                addContainerListener(broker);

                //adding all the remote endpoints that already exist.
                Object[] endpointRegistryEntrys = gigaSpace.readMultiple(new EndpointRegistryEntry(), Integer.MAX_VALUE);
                if (endpointRegistryEntrys != null && endpointRegistryEntrys.length > 0) {

                    for (Object endpointRegistryEntry : endpointRegistryEntrys) {
                        onRemoteEndpointRegistered(((EndpointRegistryEntry) endpointRegistryEntry).endpoint);
                    }
                }

                addRemoteEndpointRegistertionListener();
                // Start queue consumers for all components
                for (ComponentMBeanImpl cmp : broker.getContainer().getRegistry().getComponents()) {
                    if (cmp.isStarted()) {
                        onComponentStarted(new ComponentEvent(cmp, ComponentEvent.COMPONENT_STARTED));
                    }
                }
                // Start queue consumers for all endpoints
                ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
                for (ServiceEndpoint endpoint : endpoints) {
                    if (endpoint instanceof InternalEndpoint && ((InternalEndpoint) endpoint).isLocal()) {
                        onInternalEndpointRegistered(new EndpointEvent(endpoint,
                                EndpointEvent.INTERNAL_ENDPOINT_REGISTERED));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new JBIException("Exception caught in start: " + e.getMessage());
            }
        }
    }

    /**
     * Create and register endpoint listener.
     *
     * @param broker Broker
     */
    private void addEndpointListener(Broker broker) {
        endpointListener = new EndpointAdapter() {
            public void internalEndpointRegistered(EndpointEvent event) {
                onInternalEndpointRegistered(event);
            }

            public void internalEndpointUnregistered(EndpointEvent event) {
                onInternalEndpointUnregistered(event);
            }
        };
        broker.getContainer().addListener(endpointListener);
    }

    /**
     * Creates a {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} that consumes
     * {@link EndpointEntry} with endpoint that equals to the registered endpoint.
     *
     * Also writing {@link EndpointRegistryEntry} entry to the space, that eventually will cause other cluster members
     * to register this endpoint as extrnal endpoint.
     *
     * @param event EndpointEvent
     */
    private void onInternalEndpointRegistered(final EndpointEvent event) {
        try {
            String key = EndpointSupport.getKey(event.getEndpoint());
            if (!consumerMap.containsKey(key)) {
                MessageConsumer consumer = new MessageConsumer(new EndpointEntry(key, null));
                consumerMap.put(key, consumer);
                consumer.start();
            }
        } catch (Exception e) {
            log.error("Cannot create consumer for " + event.getEndpoint(), e);
        }


        EndpointRegistryEntry entry = new EndpointRegistryEntry();
        entry.endpoint = event.getEndpoint();
        entry.setFifo(isFifo());
        if (isPersistent()) {
            entry.makePersistent();
        } else {
            entry.makeTransient();
        }
        gigaSpace.write(entry);
        if (log.isDebugEnabled()) {
            log.debug(broker.getContainer().getName() + ": broadcasting info for " + event);
        }
    }

    /**
     * Stops a {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} from consuming
     * {@link EndpointEntry} with endpoint that matchs to the registered endpoint.
     *
     * Also taking {@link EndpointRegistryEntry} entry from the space, that eventually will cause other cluster members
     * to unregister this endpoint as extrnal endpoint.
     *
     * @param event EndpointEvent
     */
    private void onInternalEndpointUnregistered(EndpointEvent event) {
        //remove this polling container for the endpoint
        String key = EndpointSupport.getKey(event.getEndpoint());
        MessageConsumer consumer = consumerMap.remove(key);
        if (consumer != null) {
            consumer.interrupt();
        }

        EndpointRegistryEntry entry = new EndpointRegistryEntry();
        entry.endpoint = event.getEndpoint();
        entry.setFifo(isFifo());
        if (isPersistent()) {
            entry.makePersistent();
        } else {
            entry.makeTransient();
        }
        //taking EndpointRegistryEntry entry to the space, it will cause broadcast notification.
        //so other member in the cluster will remove this extrnal endpoint
        gigaSpace.take(entry);
    }

    /**
     * Create and register component listener
     *
     * @param broker Broker.
     */
    private void addComponentListener(Broker broker) {
        componentListener = new ComponentAdapter() {

            public void componentStarted(ComponentEvent event) {
                onComponentStarted(event);
            }

            public void componentStopped(ComponentEvent event) {
                onComponentStopped(event);
            }

        };
        broker.getContainer().addListener(componentListener);
    }

    /**
     * Creates a {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} that consumes
     * {@link org.openspaces.esb.servicemix.flow.ComponentEntry} with component name that matchs to the registered
     * component name.
     *
     * @param event ComponentEvent
     */
    private void onComponentStarted(final ComponentEvent event) {
        if (!started.get()) {
            return;
        }
        try {
            String key = event.getComponent().getName();
            if (!consumerMap.containsKey(key)) {
                MessageConsumer consumer = new MessageConsumer(new ComponentEntry(key, null));
                consumerMap.put(key, consumer);
                consumer.start();
            }
        } catch (Exception e) {
            log.error("Cannot create consumer for component " + event.getComponent().getName(), e);
        }
    }

    /**
     * Stops a {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} from consuming
     * {@link org.openspaces.esb.servicemix.flow.ComponentEntry} with component name that matchs to the registered
     * component name.
     *
     * @param event ComponentEvent
     */
    private void onComponentStopped(ComponentEvent event) {
        try {
            String key = event.getComponent().getName();
            MessageConsumer consumer = consumerMap.remove(key);
            if (consumer != null) {
                consumer.interrupt();
            }
        } catch (Exception e) {
            log.error("Cannot destroy consumer for component " + event.getComponent().getName(), e);
        }
    }

    /**
     * Create and register container listener.
     *
     * Creates a {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} that consumes
     * {@link ContainerEntry} with container name that matchs to the registered container name.
     *
     * @param broker Broker.
     */
    private void addContainerListener(final Broker broker) {
        String key = broker.getContainer().getName();
        if (!consumerMap.containsKey(key)) {
            MessageConsumer consumer = new MessageConsumer(new ContainerEntry(key, null));
            consumerMap.put(key, consumer);
            consumer.start();
        }
    }

    /**
     * Create and register container listener.
     *
     * Creates a {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} that consumes
     * {@link ContainerEntry} with container name that matchs to the registered container name.
     *
     * @param broker Broker.
     */
    private void removeContainerListener(final Broker broker) {
        String key = broker.getContainer().getName();
        if (!consumerMap.containsKey(key)) {
            MessageConsumer consumer = new MessageConsumer(new ContainerEntry(key, null));
            consumerMap.put(key, consumer);
            consumer.start();
        }
    }

    /**
     * Creates RemoteEventListener in order to receive notifiction when EndpointRegistryEntry registered/unregistered.
     *
     * @throws RemoteException
     * @throws TransactionException
     */
    private void addRemoteEndpointRegistertionListener() throws RemoteException, TransactionException {

        EventSessionFactory factory = EventSessionFactory.getFactory(gigaSpace.getSpace());
        EventSessionConfig config = new EventSessionConfig();
        endpointRegistrySession = factory.newDataEventSession(config, null);

        RemoteEventListener remoteEventListener = new RemoteEventListener() {

            public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
                try {
                    EntryArrivedRemoteEvent entryArrivedRemoteEvent = (EntryArrivedRemoteEvent) event;
                    EndpointRegistryEntry endpointRegistryEntry =
                            (EndpointRegistryEntry) entryArrivedRemoteEvent.getObject();
                    ServiceEndpoint endpoint = endpointRegistryEntry.endpoint;
                    NotifyActionType notifyActionType = entryArrivedRemoteEvent.getNotifyActionType();

                    if (NotifyActionType.NOTIFY_WRITE.equals(notifyActionType)) {
                        onRemoteEndpointRegistered(endpoint);

                    } else if (NotifyActionType.NOTIFY_WRITE.equals(notifyActionType)) {
                        onRemoteEndpointUnregistered(endpoint);

                    }

                } catch (net.jini.core.entry.UnusableEntryException e) {
                    log.error("Error processing incoming broadcast message, Failure to get object from event [\" + event + \"] ", e);
                }
            }
        };

        endpointRegistryRegistration = endpointRegistrySession.addListener(
                new EndpointRegistryEntry(), remoteEventListener, Lease.FOREVER, null, null,
                NotifyActionType.NOTIFY_TAKE.or(NotifyActionType.NOTIFY_WRITE));
    }

    /**
     * stop the flow
     *
     * @throws JBIException
     */
    public void stop() throws JBIException {
        if (started.compareAndSet(true, false)) {
            log.debug(broker.getContainer().getName() + ": Stopping openspaces flow");
            // wait for notifications
            try {
                removeRemoteEndpointRegistertionListener();
                // Stop queue consumers for all components
                for (ComponentMBeanImpl cmp : broker.getContainer().getRegistry().getComponents()) {
                    if (cmp.isStarted()) {
                        onComponentStopped(new ComponentEvent(cmp, ComponentEvent.COMPONENT_STOPPED));
                    }
                }
                // Stop queue consumers for all endpoints
                ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
                for (ServiceEndpoint endpoint : endpoints) {
                    if (endpoint instanceof InternalEndpoint && ((InternalEndpoint) endpoint).isLocal()) {
                        onInternalEndpointUnregistered(new EndpointEvent(endpoint,
                                EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED));
                    }
                }

                removeContainerListener(broker);
                try {
                    //give some time to the consumers to stop.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } finally {
                super.stop();
            }
        }
    }

    private void removeRemoteEndpointRegistertionListener() throws JBIException {
        try {
            endpointRegistrySession.removeListener(endpointRegistryRegistration);
            endpointRegistrySession.close();
        } catch (RemoteException e) {
            throw new JBIException(e);
        } catch (UnknownLeaseException e) {
            throw new JBIException(e);
        }
    }


    /**
     * shutDown the flow
     *
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        stop();
        super.shutDown();

        // Stop queue consumers for all endpoints
        ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
        for (ServiceEndpoint endpoint : endpoints) {
            if (endpoint instanceof InternalEndpoint && ((InternalEndpoint) endpoint).isLocal()) {
                onInternalEndpointUnregistered(new EndpointEvent(endpoint,
                        EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED));
            }
        }

        // Remove endpoint listener
        broker.getContainer().removeListener(endpointListener);
        // Remove component listener
        broker.getContainer().removeListener(componentListener);
    }

    /**
     * Register the endpoint as remote endpoint.
     *
     * @param endpoint ServiceEndpoint
     */
    public void onRemoteEndpointRegistered(ServiceEndpoint endpoint) {
        log.debug(broker.getContainer().getName() + ": adding remote endpoint: " + endpoint);
        broker.getContainer().getRegistry().registerRemoteEndpoint(endpoint);
    }

    /**
     * Unregister the endpoint as remote endpoint.
     *
     * @param endpoint ServiceEndpoint
     */
    public void onRemoteEndpointUnregistered(ServiceEndpoint endpoint) {

        log.debug(broker.getContainer().getName() + ": removing remote endpoint: " + endpoint);
        broker.getContainer().getRegistry().unregisterRemoteEndpoint(endpoint);
    }

    /**
     * @param me MessageExchang
     * @throws javax.jbi.messaging.MessagingException
     *
     * @see super#doSend(org.apache.servicemix.jbi.messaging.MessageExchangeImpl)
     */
    protected void doSend(MessageExchangeImpl me) throws MessagingException {
        doRouting(me);
    }

    /**
     * @param me MessageExchange
     * @throws MessagingException
     * @see super#doRouting(org.apache.servicemix.jbi.messaging.MessageExchangeImpl)
     */
    public void doRouting(MessageExchangeImpl me) throws MessagingException {
        try {
            MetaDataEntry entry;
            if (me.getRole() == MessageExchange.Role.PROVIDER) {

                //case sending request.
                if (me.getDestinationId() == null) {
                    entry = new EndpointEntry(EndpointSupport.getKey(me.getEndpoint()), me);
                } else if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_PROVIDER)) && !isSynchronous(me)) {
                    entry = new ComponentEntry(me.getDestinationId().getName(), me);
                } else {
                    entry = new ContainerEntry(me.getDestinationId().getContainerName(), me);
                }
            } else {
                //case sending response.

                if (me.getSourceId() == null) {
                    throw new IllegalStateException("No sourceId set on the exchange");
                } else
                    //todo: bug fix isSynchronous(me)  will always return false...
                    if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_CONSUMER)) && !isSynchronous(me)) {
                        // If the consumer is stateless and has specified a sender
                        // component,
                        // this exchange will be sent to the given component queue,
                        // so that
                        // fail-over and load-balancing can be achieved
                        // This property must have been created using
                        // EndpointSupport.getKey
                        if (me.getProperty(JbiConstants.SENDER_ENDPOINT) != null) {
                            entry = new EndpointEntry((String) me.getProperty(JbiConstants.SENDER_ENDPOINT), me);
                        } else {
                            entry = new ComponentEntry(me.getSourceId().getName(), me);
                        }
                    } else {
                        entry = new ContainerEntry(me.getSourceId().getContainerName(), me);
                    }
            }
            entry.setFifo(isFifo());
            if (isPersistent()) {
                entry.makePersistent();
            } else {
                entry.makeTransient();
            }
            gigaSpace.write(entry);

        } catch (Exception e) {
            log.error("Failed to send exchange: " + me + " internal OpenSpaces Network", e);
            throw new MessagingException(e);
        }
    }

    /**
     * Invoked by {@link org.openspaces.esb.servicemix.flow.OpenSpacesFlow.MessageConsumer} whenever
     * {@link org.openspaces.esb.servicemix.flow.MessageExchangeEntry} took from the space.
     *
     * Routes the messageExchangeEntry.message {@link javax.jbi.messaging.MessageExchange}.
     *
     * @param messageExchangeEntry MessageExchangeEntry
     */
    public void onEvent(MessageExchangeEntry messageExchangeEntry) {
        try {
            if (started.get()) {
                final MessageExchangeImpl me = (MessageExchangeImpl) messageExchangeEntry.message;
                // Dispatch the message in another thread so as to free the openspaces session
                // else if a component do a sendSync into the openspaces flow,
                // the whole flow is deadlocked
                executor.execute(new Runnable() {
                    public void run() {
                        try {
                            if (me.getDestinationId() == null) {
                                ServiceEndpoint se = me.getEndpoint();
                                se = broker.getContainer().getRegistry().getInternalEndpoint(se.getServiceName(),
                                        se.getEndpointName());
                                me.setEndpoint(se);
                                me.setDestinationId(((InternalEndpoint) se).getComponentNameSpace());
                            }
                            OpenSpacesFlow.super.doRouting(me);
                        } catch (Throwable e) {
                            log.error("Caught an exception routing ExchangePacket: ", e);
                        }
                    }
                });
            }

        } catch (Exception e) {
            log.error("Caught an exception unpacking OpenSpaces Message: ", e);
        }
    }

    /**
     * @return true if the messages orderd.
     */
    public boolean isFifo() {
        return fifo;
    }

    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    /**
     * @return true if the messages persistent
     */
    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * The MessageConsumer pulls {@link org.openspaces.esb.servicemix.flow.MessageExchangeEntry} and invoke the onEvent
     * method in order to route the incoming messages.
     */
    private class MessageConsumer extends Thread {

        private volatile boolean working = true;

        private MessageExchangeEntry messageExchangeEntryTemplate;

        MessageConsumer(MessageExchangeEntry messageExchangeEntry) {
            this.messageExchangeEntryTemplate = messageExchangeEntry;
        }


        @Override
        public void run() {
            while (working) {
                MessageExchangeEntry messageExchangeEntry = null;
                try {
                    messageExchangeEntry = gigaSpace.take(messageExchangeEntryTemplate, timeout);
                } catch (SpaceInterruptedException e) {
                    //Finish the execution of this thread.
                    return;
                } catch (DataAccessException e) {
                    log.error("error occured while taking from space.", e);
                }
                if (messageExchangeEntry != null) {
                    onEvent(messageExchangeEntry);
                }
            }

        }

        @Override
        public void interrupt() {
            working = false;
            super.interrupt();
        }
    }
}
