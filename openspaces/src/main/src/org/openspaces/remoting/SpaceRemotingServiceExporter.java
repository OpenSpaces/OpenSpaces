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

package org.openspaces.remoting;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.FilterProvider;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.events.EventTemplateProvider;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Exports a list of services (beans) as remote services with the Space as the transport layer. All
 * the interfaces each service implements are registered as lookup names (matching
 * {@link SpaceRemotingInvocation#getLookupName()} which are then used to lookup the actual service
 * when a remote invocation is received. The correct service and its method are then executed and a
 * {@link org.openspaces.remoting.AsyncSpaceRemotingEntry} is written back to the space. The remote result
 * can either hold the return value (or <code>null</code> in case of void return value) or an
 * exception that was thrown by the service.
 *
 * <p>The exporter implements {@link org.openspaces.events.SpaceDataEventListener} which means that it
 * acts as a listener to data events and should be used with the different event containers such as
 * {@link org.openspaces.events.polling.SimplePollingEventListenerContainer}. This method of execution
 * is called <b>async</b> remote execution.
 *
 * <p>It also implements {@link org.openspaces.events.EventTemplateProvider} which means that within
 * the event container configuration there is no need to configure the template, as it uses the one
 * provided by this exported.
 *
 * <p>The exporter also implements {@link org.openspaces.core.space.filter.FilterProviderFactory} and
 * allows to execute services in a <b>sync</b> manner.
 *
 * @author kimchy
 * @see org.openspaces.events.polling.SimplePollingEventListenerContainer
 * @see org.openspaces.remoting.AsyncSpaceRemotingEntry
 * @see AsyncSpaceRemotingProxyFactoryBean
 */
public class SpaceRemotingServiceExporter implements SpaceDataEventListener<AsyncSpaceRemotingEntry>, InitializingBean, ApplicationContextAware,
        EventTemplateProvider, FilterProviderFactory, ClusterInfoAware {

    public static final String DEFAULT_ASYNC_INTERFACE_SUFFIX = "Async";

    private static final Log logger = LogFactory.getLog(SpaceRemotingServiceExporter.class);

    private List<Object> services;

    private ApplicationContext applicationContext;

    private Map<String, Object> interfaceToService = new HashMap<String, Object>();

    private String asyncInterfaceSuffix = DEFAULT_ASYNC_INTERFACE_SUFFIX;

    private boolean fifo = false;

    // sync execution fields

    private FilterProvider filterProvider;


    private ClusterInfo clusterInfo;

    /**
     * Sets the list of services that will be exported as remote services. Each service will have
     * all of its interfaces registered as lookups (mapping to
     * {@link AsyncSpaceRemotingEntry#getLookupName()} which will then be used to invoke the correct
     * service.
     */
    public void setServices(List<Object> services) {
        this.services = services;
    }

    /**
     * For async based execution of remote services, this is one of the options to enable this by
     * using two different interfaces. The first is the actual "server side" interface (sync), and
     * the other has the same interface name just with an "async suffix" to it. The exporter will
     * identify the async suffix, and will perform the invocation on the actual interface.
     *
     * <p>This setter allows to set the async suffix which by default is <code>Async</code>.
     */
    public void setAsyncInterfaceSuffix(String asyncInterfaceSuffix) {
        this.asyncInterfaceSuffix = asyncInterfaceSuffix;
    }

    /**
     * Sets the template used to read async invocation (the {@link org.openspaces.remoting.AsyncSpaceRemotingEntry})
     * to be fifo. Works in with setting the {@link org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean}
     * fifo flag to <code>true</code> and allows for remoting to work in fifo mode without needing to set the whole
     * Space to work in fifo mode.
     */
    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    /**
     * Application context injected by Spring
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Cluster Info injected
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(services, "services property is required");
        // go over the services and create the interface to service lookup
        for (Object service : services) {
            Class<?>[] interfaces = ClassUtils.getAllInterfaces(service);
            for (Class<?> anInterface : interfaces) {
                interfaceToService.put(anInterface.getName(), service);
            }
        }

        // create the filter provider
        filterProvider = new FilterProvider("Remoting Filter", new RemotingServiceInvoker());
        filterProvider.setActiveWhenBackup(false);
        filterProvider.setEnabled(true);
        filterProvider.setOpCodes(new int[]{FilterOperationCodes.BEFORE_READ_MULTIPLE, FilterOperationCodes.BEFORE_TAKE_MULTIPLE});
    }

    /**
     * The template used for receiving events. Defaults to all objects that are of type
     * {@link AsyncSpaceRemotingEntry}.
     */
    public Object getTemplate() {
        AsyncSpaceRemotingEntry remotingEntry = new AsyncSpaceRemotingEntry();
        remotingEntry.isInvocation = true;
        remotingEntry.setFifo(fifo);
        return remotingEntry;
    }

    /**
     * Receives a {@link org.openspaces.remoting.AsyncSpaceRemotingEntry} which holds all the relevant
     * invocation information. Looks up (based on {@link AsyncSpaceRemotingEntry#getLookupName()}
     * the interface the service is registered against (which is the interface the service
     * implements) and then invokes the relevant method within it using the provided method name and
     * arguments. Write the result value or invocation exception back to the space using
     * {@link org.openspaces.remoting.AsyncSpaceRemotingEntry}.
     *
     * @param remotingEntry The remote entry object
     * @param gigaSpace     The GigaSpace interface
     * @param txStatus      A transactional status
     * @param source        An optional source event information
     */
    public void onEvent(AsyncSpaceRemotingEntry remotingEntry, GigaSpace gigaSpace, TransactionStatus txStatus, Object source)
            throws RemoteAccessException {

        String lookupName = remotingEntry.lookupName;
        if (lookupName.endsWith(asyncInterfaceSuffix)) {
            lookupName = lookupName.substring(0, lookupName.length() - asyncInterfaceSuffix.length());
        }

        Object service = interfaceToService.get(lookupName);
        if (service == null) {
            // we did not get an interface, maybe it is a bean name?
            service = applicationContext.getBean(lookupName);
            if (service == null) {
                writeResponse(gigaSpace, remotingEntry, new RemoteLookupFailureException(
                        "Failed to find service for lookup [" + remotingEntry.lookupName + "]"));
                return;
            }
        }

        // TODO could so some caching of the method invoker for better performance
        Object[] arguments = remotingEntry.arguments;
        if (arguments == null) {
            arguments = new Object[0];
        }
        Class<?>[] argumentTypes = new Class<?>[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argumentTypes[i] = (arguments[i] != null ? arguments[i].getClass() : Object.class);
        }

        Method method;
        try {
            method = service.getClass().getMethod(remotingEntry.methodName, argumentTypes);
        } catch (Exception e) {
            writeResponse(gigaSpace, remotingEntry, new RemoteLookupFailureException("Failed to find method ["
                    + remotingEntry.methodName + "] for lookup [" + remotingEntry.lookupName + "]", e));
            return;
        }
        try {
            Object retVal = method.invoke(service, arguments);
            writeResponse(gigaSpace, remotingEntry, retVal);
        } catch (InvocationTargetException e) {
            writeResponse(gigaSpace, remotingEntry, e.getTargetException());
        } catch (IllegalAccessException e) {
            writeResponse(gigaSpace, remotingEntry, new RemoteLookupFailureException("Failed to access method ["
                    + remotingEntry.methodName + "] for lookup [" + remotingEntry.lookupName + "]", e));
        }
    }

    @SuppressWarnings("unchecked")
    private void writeResponse(GigaSpace gigaSpace, AsyncSpaceRemotingEntry remotingEntry, Throwable e) {
        if (remotingEntry.oneWay == null || !remotingEntry.oneWay) {
            AsyncSpaceRemotingEntry result = remotingEntry.buildResult(e);
            if (clusterInfo != null) {
                result.instanceId = clusterInfo.getInstanceId();
            }
            gigaSpace.write(result);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Remoting execution is configured as one way and an exception was thrown", e);
            }
        }
    }

    private void writeResponse(GigaSpace gigaSpace, AsyncSpaceRemotingEntry remotingEntry, Object retVal) {
        if (remotingEntry.oneWay == null || !remotingEntry.oneWay) {
            AsyncSpaceRemotingEntry result = remotingEntry.buildResult(retVal);
            if (clusterInfo != null) {
                result.instanceId = clusterInfo.getInstanceId();
            }
            gigaSpace.write(result);
        }
    }

    // Sync execution

    /**
     * Returns an instance of {@link org.openspaces.remoting.SpaceRemotingServiceExporter.RemotingServiceInvoker}
     * filter for sync remote service execution.
     */
    public FilterProvider getFilterProvider() {
        return this.filterProvider;
    }

    /**
     * The remoting service invoker is a Space filter that acts as means to preform sync remote service
     * execution including broadcast execution. The filter registers for
     * {@link com.j_spaces.core.filters.FilterOperationCodes#BEFORE_READ_MULTIPLE} and
     * {@link com.j_spaces.core.filters.FilterOperationCodes#BEFORE_TAKE_MULTIPLE} operations and uses the
     * read or take multiple template as an entry holding the actual invocation information (the template is
     * {@link org.openspaces.remoting.SyncSpaceRemotingEntry}). The filter executes the service required
     * and if it is not one way, it will write back the result as {@link org.openspaces.remoting.SyncSpaceRemotingEntry}
     * back to the Space. It also changes the provided template to match against a unique id generated when writing
     * the result to the Space. The take multiple operation is then executed and takes the result from the Space
     * and returns it to the calling client.
     */
    private class RemotingServiceInvoker implements ISpaceFilter {

        private final String SPACE_REMOTING_ENTRY_CLASSNAME = SyncSpaceRemotingEntry.class.getName();

        private IJSpace space;

        private AtomicLong idGenerator = new AtomicLong();

        public void init(IJSpace space, String filterId, String url, int priority) throws RuntimeException {
            this.space = space;
        }

        public void process(SpaceContext context, ISpaceFilterEntry entry, int operationCode) throws RuntimeException {
            switch (operationCode) {
                case FilterOperationCodes.BEFORE_READ_MULTIPLE:
                case FilterOperationCodes.BEFORE_TAKE_MULTIPLE:
                    break;
                default:
                    return;
            }
            if (!SPACE_REMOTING_ENTRY_CLASSNAME.equals(entry.getClassName())) {
                return;
            }
            SyncSpaceRemotingEntry remotingEntry;
            try {
                remotingEntry = (SyncSpaceRemotingEntry) entry.getObject(space);
            } catch (UnusableEntryException e) {
                logger.error("Faied to get actual object for [" + entry.getClassName() + "], ignoring sync remoting invocation", e);
                return;
            }

            // Reset fields to perform matching
            entry.setFieldValue("lookupName", null);
            entry.setFieldValue("methodName", null);
            entry.setFieldValue("arguments", null);
            entry.setFieldValue("oneWay", null);


            Object service = interfaceToService.get(remotingEntry.lookupName);
            if (service == null) {
                // we did not get an interface, maybe it is a bean name?
                service = applicationContext.getBean(remotingEntry.lookupName);
                if (service == null) {
                    writeResponse(space, entry, remotingEntry, new RemoteLookupFailureException(
                            "Failed to find service for lookup [" + remotingEntry.getLookupName() + "]"));
                    return;
                }
            }

            Object[] arguments = remotingEntry.arguments;
            if (arguments == null) {
                arguments = new Object[0];
            }
            Class<?>[] argumentTypes = new Class<?>[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                argumentTypes[i] = (arguments[i] != null ? arguments[i].getClass() : Object.class);
            }

            Method method;
            try {
                method = service.getClass().getMethod(remotingEntry.methodName, argumentTypes);
            } catch (Exception e) {
                writeResponse(space, entry, remotingEntry, new RemoteLookupFailureException("Failed to find method ["
                        + remotingEntry.getMethodName() + "] for lookup [" + remotingEntry.getLookupName() + "]", e));
                return;
            }
            try {
                Object retVal = method.invoke(service, arguments);
                writeResponse(space, entry, remotingEntry, retVal);
            } catch (InvocationTargetException e) {
                writeResponse(space, entry, remotingEntry, e.getTargetException());
            } catch (IllegalAccessException e) {
                writeResponse(space, entry, remotingEntry, new RemoteLookupFailureException("Failed to access method ["
                        + remotingEntry.getMethodName() + "] for lookup [" + remotingEntry.getLookupName() + "]", e));
            }
        }

        public void process(SpaceContext context, ISpaceFilterEntry[] entries, int operationCode) throws RuntimeException {

        }

        public void close() throws RuntimeException {

        }

        @SuppressWarnings("unchecked")
        private void writeResponse(IJSpace space, ISpaceFilterEntry entry, SyncSpaceRemotingEntry remotingEntry, Throwable e) {
            if (remotingEntry.oneWay == null || !remotingEntry.oneWay) {
                try {
                    remotingEntry = remotingEntry.buildResult(e);
                    setGeneraterdUID(remotingEntry, entry);
                    if (clusterInfo != null) {
                        remotingEntry.instanceId = clusterInfo.getInstanceId();
                    }
                    space.write(remotingEntry, null, Lease.FOREVER);
                } catch (Exception e1) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Failed to write remoting entry with exception [" + e.getMessage() + "]", e1);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The actual exception is", e);
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Remoting execution is configured as one way and an exception was thrown", e);
                }
            }
        }

        private void writeResponse(IJSpace space, ISpaceFilterEntry entry, SyncSpaceRemotingEntry remotingEntry, Object retVal) {
            if (remotingEntry.oneWay == null || !remotingEntry.oneWay) {
                try {
                    remotingEntry = remotingEntry.buildResult(retVal);
                    setGeneraterdUID(remotingEntry, entry);
                    if (clusterInfo != null) {
                        remotingEntry.instanceId = clusterInfo.getInstanceId();
                    }
                    space.write(remotingEntry, null, Lease.FOREVER);
                } catch (Exception e1) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Failed to write remoting entry", e1);
                    }
                }
            }
        }

        private void setGeneraterdUID(SyncSpaceRemotingEntry remotingEntry, ISpaceFilterEntry entry) {
            remotingEntry.uid = idGenerator.incrementAndGet();
            entry.setFieldValue("uid", remotingEntry.uid);
        }
    }
}
