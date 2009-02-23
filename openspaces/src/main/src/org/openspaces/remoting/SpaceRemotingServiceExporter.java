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

import com.gigaspaces.reflect.IMethod;
import com.gigaspaces.reflect.ReflectionUtil;
import com.gigaspaces.reflect.standard.StandardMethod;
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
import org.openspaces.core.transaction.manager.ExistingJiniTransactionManager;
import org.openspaces.events.EventTemplateProvider;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
 * is called <b>async</b> remote execution ({@link org.openspaces.remoting.AsyncSpaceRemotingProxyFactoryBean}).
 *
 * <p>It also implements {@link org.openspaces.events.EventTemplateProvider} which means that within
 * the event container configuration there is no need to configure the template, as it uses the one
 * provided by this exported.
 *
 * <p>The exporter also implements {@link org.openspaces.core.space.filter.FilterProviderFactory} and
 * allows to execute services in a <b>sync</b> manner ({@link org.openspaces.remoting.SyncSpaceRemotingProxyFactoryBean}).
 *
 * <p>Last, the exporter provides services to executor based remoting ({@link org.openspaces.remoting.ExecutorSpaceRemotingProxyFactoryBean}).
 *
 * <p>By default, the exporter will also autowire and post process all the arguments passed, allowing
 * to inject them with "server" side beans using Spring {@link org.springframework.beans.factory.annotation.Autowired}
 * annotation for example. Note, this variables must be defined as <code>transient</code> so they won't be
 * passed back to the client. This can be disabled by setting {@link #setDisableAutowiredArguements(boolean)}
 * to <code>true</code>.
 *
 * @author kimchy
 * @see org.openspaces.events.polling.SimplePollingEventListenerContainer
 * @see org.openspaces.remoting.AsyncSpaceRemotingEntry
 * @see AsyncSpaceRemotingProxyFactoryBean
 */
public class SpaceRemotingServiceExporter implements SpaceDataEventListener<AsyncSpaceRemotingEntry>, InitializingBean, ApplicationContextAware, BeanNameAware,
        EventTemplateProvider, FilterProviderFactory, ClusterInfoAware, ApplicationListener, ServiceDetailsProvider, ServiceMonitorsProvider {

    public static final String DEFAULT_ASYNC_INTERFACE_SUFFIX = "Async";

    private static final Log logger = LogFactory.getLog(SpaceRemotingServiceExporter.class);

    private List<Object> services = new ArrayList<Object>();

    private List<ServiceInfo> servicesInfo = new ArrayList<ServiceInfo>();

    private IdentityHashMap<Object, ServiceInfo> serviceToServiceInfoMap = new IdentityHashMap<Object, ServiceInfo>();

    private AtomicLong processed = new AtomicLong();

    private AtomicLong failed = new AtomicLong();

    private ApplicationContext applicationContext;

    private String beanName;

    private Map<String, Object> interfaceToService = new HashMap<String, Object>();

    private String asyncInterfaceSuffix = DEFAULT_ASYNC_INTERFACE_SUFFIX;

    private boolean fifo = false;

    private boolean disableAutowiredArguements = false;

    private ServiceExecutionAspect serviceExecutionAspect;

    private String templateLookupName;

    // sync execution fields

    private long syncEntryWriteLease = Lease.FOREVER;

    private FilterProvider filterProvider;


    private ClusterInfo clusterInfo;

    private MethodInvocationCache methodInvocationCache = new MethodInvocationCache();

    private volatile boolean initialized = false;

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
     * Allows to disable (by default it is enabled) the autowiring of method arguments with beans that
     * exists within the server side context.
     */
    public void setDisableAutowiredArguements(boolean disableAutowiredArguements) {
        this.disableAutowiredArguements = disableAutowiredArguements;
    }

    /**
     * Allows to inject a service execution callback.
     */
    public void setServiceExecutionAspect(ServiceExecutionAspect serviceExecutionAspect) {
        this.serviceExecutionAspect = serviceExecutionAspect;
    }

    /**
     * Sets the sync entry write lease. Defaults to <code>Lease.FOREVER</code>.
     */
    public void setSyncEntryWriteLease(long syncEntryWriteLease) {
        this.syncEntryWriteLease = syncEntryWriteLease;
    }

    /**
     * Allows to narrow down the async polling container to perform a lookup only on specific lookup
     * name (which is usually the interface that will be used to proxy it on the client side). Defaults
     * to match on all async remoting invocations.
     *
     * <p>This option allows to create several polling container, each for different service that will
     * perform the actual invocation.
     */
    public void setTemplateLookupName(String templateLookupName) {
        this.templateLookupName = templateLookupName;
    }

    /**
     * Application context injected by Spring
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * Cluster Info injected
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void addService(String beanId, Object service) throws IllegalStateException {
        if (initialized) {
            throw new IllegalStateException("Can't add a service once the exporter has initialized");
        }
        this.servicesInfo.add(new ServiceInfo(beanId, service.getClass().getName(), service));
    }

    public void afterPropertiesSet() throws Exception {
        // create the filter provider
        filterProvider = new FilterProvider("Remoting Filter", new RemotingServiceInvoker());
        filterProvider.setActiveWhenBackup(false);
        filterProvider.setEnabled(true);
        filterProvider.setOpCodes(FilterOperationCodes.BEFORE_READ_MULTIPLE, FilterOperationCodes.BEFORE_TAKE_MULTIPLE);
        if (beanName == null) {
            beanName = "serviceExporter";
        }
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            initialized = true;
            Assert.notNull(services, "services property is required");
            // go over the services and create the interface to service lookup
            int naCounter = 0;
            for (Object service : services) {
                if (service instanceof ServiceRef) {
                    String ref = ((ServiceRef) service).getRef();
                    service = applicationContext.getBean(ref);
                    this.servicesInfo.add(new ServiceInfo(ref, service.getClass().getName(), service));
                } else {
                    this.servicesInfo.add(new ServiceInfo("NA" + (++naCounter), service.getClass().getName(), service));
                }
            }
            for (ServiceInfo serviceInfo : servicesInfo) {
                Class<?>[] interfaces = ClassUtils.getAllInterfaces(serviceInfo.getService());
                for (Class<?> anInterface : interfaces) {
                    interfaceToService.put(anInterface.getName(), serviceInfo.getService());
                    methodInvocationCache.addService(anInterface, serviceInfo.getService());
                }

                serviceToServiceInfoMap.put(serviceInfo.getService(), serviceInfo);
            }
        }
    }

    /**
     * The template used for receiving events. Defaults to all objects that are of type
     * {@link AsyncSpaceRemotingEntry}.
     */
    public Object getTemplate() {
        AsyncSpaceRemotingEntry remotingEntry = new AsyncSpaceRemotingEntry();
        remotingEntry.isInvocation = true;
        remotingEntry.setFifo(fifo);
        remotingEntry.lookupName = templateLookupName;
        if (logger.isDebugEnabled()) {
            logger.debug("Registering async remoting service tempalte [" + remotingEntry + "]");
        }
        return remotingEntry;
    }

    public ServiceDetails[] getServicesDetails() {
        ArrayList<RemotingServiceDetails.RemoteService> remoteServices = new ArrayList<RemotingServiceDetails.RemoteService>();
        for (ServiceInfo serviceInfo : servicesInfo) {
            remoteServices.add(new RemotingServiceDetails.RemoteService(serviceInfo.getBeanId(), serviceInfo.getClassName()));
        }
        return new ServiceDetails[] {new RemotingServiceDetails(beanName, remoteServices.toArray(new RemotingServiceDetails.RemoteService[remoteServices.size()]))};
    }

    public ServiceMonitors[] getServicesMonitors() {
        ArrayList<RemotingServiceMonitors.RemoteServiceStats> remoteServiceStats = new ArrayList<RemotingServiceMonitors.RemoteServiceStats>();
        for (ServiceInfo serviceInfo : servicesInfo) {
            remoteServiceStats.add(new RemotingServiceMonitors.RemoteServiceStats(serviceInfo.getBeanId(), serviceInfo.getProcessed().get(), serviceInfo.getFailures().get()));
        }
        return new ServiceMonitors[] {new RemotingServiceMonitors(beanName, processed.get(), failed.get(), remoteServiceStats.toArray(new RemotingServiceMonitors.RemoteServiceStats[remoteServiceStats.size()]))};
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
            try {
                service = applicationContext.getBean(lookupName);
            } catch (NoSuchBeanDefinitionException e) {
                // do nothing, write back a proper exception
            }
            if (service == null) {
                writeResponse(gigaSpace, remotingEntry, new RemoteLookupFailureException(
                        "Failed to find service for lookup [" + remotingEntry.lookupName + "]"));
                return;
            }
        }

        autowireArguments(service, remotingEntry.getArguments());

        IMethod method;
        try {
            method = methodInvocationCache.findMethod(lookupName, service, remotingEntry.methodName, remotingEntry.arguments);
        } catch (Exception e) {
            failedExecution(service);
            writeResponse(gigaSpace, remotingEntry, new RemoteLookupFailureException("Failed to find method ["
                    + remotingEntry.methodName + "] for lookup [" + remotingEntry.lookupName + "]", e));
            return;
        }
        try {
            Object retVal;
            if (serviceExecutionAspect != null) {
                retVal = serviceExecutionAspect.invoke(remotingEntry, method, service);
            } else {
                retVal = method.invoke(service, remotingEntry.arguments);
            }
            writeResponse(gigaSpace, remotingEntry, retVal);
            processedExecution(service);
        } catch (InvocationTargetException e) {
            failedExecution(service);
            writeResponse(gigaSpace, remotingEntry, e.getTargetException());
        } catch (IllegalAccessException e) {
            failedExecution(service);
            writeResponse(gigaSpace, remotingEntry, new RemoteLookupFailureException("Failed to access method ["
                    + remotingEntry.methodName + "] for lookup [" + remotingEntry.lookupName + "]", e));
        } catch (Throwable e) {
            failedExecution(service);
            writeResponse(gigaSpace, remotingEntry, e);
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

    private void autowireArguments(Object service, Object[] args) {
        if (disableAutowiredArguements) {
            return;
        }
        if (args == null) {
            return;
        }
        if (shouldAutowire(service)) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBeanProperties(arg, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
                beanFactory.initializeBean(arg, arg.getClass().getName());
            }
        }
    }

    private boolean shouldAutowire(Object service) {
        if (service instanceof AutowireArgumentsMarker) {
            return true;
        }
        if (service.getClass().isAnnotationPresent(AutowireArguments.class)) {
            return true;
        }
        for (Class clazz : service.getClass().getInterfaces()) {
            if (clazz.isAnnotationPresent(AutowireArguments.class)) {
                return true;
            }
        }
        return false;
    }

    // Executor execution

    public Object invokeExecutor(ExecutorRemotingTask task) throws Throwable {
        String lookupName = task.getLookupName();
        if (lookupName.endsWith(asyncInterfaceSuffix)) {
            lookupName = lookupName.substring(0, lookupName.length() - asyncInterfaceSuffix.length());
        }

        Object service = interfaceToService.get(lookupName);
        if (service == null) {
            // we did not get an interface, maybe it is a bean name?
            try {
                service = applicationContext.getBean(lookupName);
            } catch (NoSuchBeanDefinitionException e) {
                // do nothing, write back a proper exception
            }
            if (service == null) {
                throw new RemoteLookupFailureException("Failed to find service for lookup [" + task.getLookupName() + "]");
            }
        }

        autowireArguments(service, task.getArguments());

        IMethod method;
        try {
            method = methodInvocationCache.findMethod(lookupName, service, task.getMethodName(), task.getArguments());
        } catch (Exception e) {
            failedExecution(service);
            throw new RemoteLookupFailureException("Failed to find method [" + task.getMethodName() + "] for lookup [" + task.getLookupName() + "]");
        }
        try {
            Object retVal;
            if (serviceExecutionAspect != null) {
                retVal = serviceExecutionAspect.invoke(task, method, service);
            } else {
                retVal = method.invoke(service, task.getArguments());
            }
            processedExecution(service);
            return retVal;
        } catch (InvocationTargetException e) {
            failedExecution(service);
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            failedExecution(service);
            throw new RemoteLookupFailureException("Failed to access method [" + task.getMethodName() + "] for lookup [" + task.getLookupName() + "]");
        }
    }

    private void processedExecution(Object service) {
        processed.incrementAndGet();
        serviceToServiceInfoMap.get(service).getProcessed().incrementAndGet();
    }

    private void failedExecution(Object service) {
        failed.incrementAndGet();
        serviceToServiceInfoMap.get(service).getFailures().incrementAndGet();
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
                logger.error("Failed to get actual object for [" + entry.getClassName() + "], ignoring sync remoting invocation", e);
                return;
            }

            // Reset fields to perform matching
            entry.setFieldValue("lookupName", null);
            entry.setFieldValue("methodName", null);
            entry.setFieldValue("arguments", null);
            entry.setFieldValue("oneWay", null);


            String lookupName = remotingEntry.lookupName;
            if (lookupName.endsWith(asyncInterfaceSuffix)) {
                lookupName = lookupName.substring(0, lookupName.length() - asyncInterfaceSuffix.length());
            }
            Object service = interfaceToService.get(lookupName);
            if (service == null) {
                // we did not get an interface, maybe it is a bean name?
                try {
                    service = applicationContext.getBean(lookupName);
                } catch (NoSuchBeanDefinitionException e) {
                    // do nothing, return a proper response
                }
                if (service == null) {
                    writeResponse(space, entry, remotingEntry, new RemoteLookupFailureException(
                            "Failed to find service for lookup [" + remotingEntry.getLookupName() + "]"));
                    return;
                }
            }

            autowireArguments(service, remotingEntry.getArguments());

            // bind current transaction
            boolean boundedTransaction = ExistingJiniTransactionManager.bindExistingTransaction(remotingEntry.transaction);
            try {
                IMethod method;
                try {
                    method = methodInvocationCache.findMethod(lookupName, service, remotingEntry.methodName, remotingEntry.arguments);
                } catch (Exception e) {
                    failedExecution(service);
                    writeResponse(space, entry, remotingEntry, new RemoteLookupFailureException("Failed to find method ["
                            + remotingEntry.getMethodName() + "] for lookup [" + remotingEntry.getLookupName() + "]", e));
                    return;
                }
                try {
                    Object retVal;
                    if (serviceExecutionAspect != null) {
                        retVal = serviceExecutionAspect.invoke(remotingEntry, method, service);
                    } else {
                        retVal = method.invoke(service, remotingEntry.arguments);
                    }
                    writeResponse(space, entry, remotingEntry, retVal);
                    processedExecution(service);
                } catch (InvocationTargetException e) {
                    failedExecution(service);
                    writeResponse(space, entry, remotingEntry, e.getTargetException());
                } catch (IllegalAccessException e) {
                    failedExecution(service);
                    writeResponse(space, entry, remotingEntry, new RemoteLookupFailureException("Failed to access method ["
                            + remotingEntry.getMethodName() + "] for lookup [" + remotingEntry.getLookupName() + "]", e));
                } catch (Throwable e) {
                    failedExecution(service);
                    writeResponse(space, entry, remotingEntry, e);
                }
            } finally {
                if (boundedTransaction) {
                    ExistingJiniTransactionManager.unbindExistingTransaction();
                }
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
                    space.write(remotingEntry, null, syncEntryWriteLease);
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
                    space.write(remotingEntry, null, syncEntryWriteLease);
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

    /**
     * Holds a cache of method reflection information per service (interface). If there is a single method
     * within the interface with the same name and number of parameters, then the cached version will be used.
     * If there are more than one method with the name and number of parameteres, then the Java reflection
     * <code>getMethod</code> will be used.
     *
     * <p>Note, as a side effect, if we are using cached methods, we support executing interfaces that declare
     * a super type as a parameter, and invocation will be done using a sub type. This does not work with
     * Java reflection getMethod as it only returns exact match for argument types.
     *
     * <p>Also note, this cache is *not* thread safe. The idea here is that this cache is initlaized at startup
     * and then never updated.
     */
    private class MethodInvocationCache {

        private Map<String, MethodsCacheEntry> serviceToMethodCacheMap = new HashMap<String, MethodsCacheEntry>();

        public IMethod findMethod(String lookupName, Object service, String methodName, Object[] arguments) throws NoSuchMethodException {
            int numberOfParameters = 0;
            if (arguments != null) {
                numberOfParameters = arguments.length;
            }
            IMethod invocationMethod;
            IMethod[] methods = serviceToMethodCacheMap.get(lookupName).getMethodCacheEntry(methodName).getMethod(numberOfParameters);
            if (methods != null && methods.length == 1) {
                //we can do caching
                invocationMethod = methods[0];
            } else {
                if (arguments == null) {
                    arguments = new Object[0];
                }
                Class<?>[] argumentTypes = new Class<?>[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    argumentTypes[i] = (arguments[i] != null ? arguments[i].getClass() : Object.class);
                }

                invocationMethod = new StandardMethod(service.getClass().getMethod(methodName, argumentTypes));
            }
            return invocationMethod;
        }

        public void addService(Class serviceInterface, Object service) {
            MethodsCacheEntry methodsCacheEntry = new MethodsCacheEntry();
            serviceToMethodCacheMap.put(serviceInterface.getName(), methodsCacheEntry);
            methodsCacheEntry.addService(service.getClass());
        }

        private class MethodsCacheEntry {

            private Map<String, MethodCacheEntry> methodNameMap = new HashMap<String, MethodCacheEntry>();

            public MethodCacheEntry getMethodCacheEntry(String methodName) {
                return methodNameMap.get(methodName);
            }

            public void addService(Class service) {
                Method[] methods = service.getMethods();
                for (Method method : methods) {
                    MethodCacheEntry methodCacheEntry = methodNameMap.get(method.getName());
                    if (methodCacheEntry == null) {
                        methodCacheEntry = new MethodCacheEntry();
                        methodNameMap.put(method.getName(), methodCacheEntry);
                    }
                    methodCacheEntry.addMethod(method);
                }
            }
        }

        private class MethodCacheEntry {

            private Map<Integer, IMethod[]> parametersPerMethodMap = new HashMap<Integer, IMethod[]>();

            public IMethod[] getMethod(int numberOfParams) {
                return parametersPerMethodMap.get(numberOfParams);
            }

            public void addMethod(Method method) {
                IMethod fastMethod = ReflectionUtil.createMethod(method);
                IMethod[] list = parametersPerMethodMap.get(method.getParameterTypes().length);
                if (list == null) {
                    list = new IMethod[]{fastMethod};
                } else {
                    IMethod[] tempList = new IMethod[list.length + 1];
                    System.arraycopy(list, 0, tempList, 0, list.length);
                    tempList[list.length] = fastMethod;
                    list = tempList;
                }
                parametersPerMethodMap.put(method.getParameterTypes().length, list);
            }
        }
    }

    private static class ServiceInfo {
        private final String beanId;
        private final String className;
        private final Object service;
        private final AtomicLong processed = new AtomicLong();
        private final AtomicLong failures = new AtomicLong();

        private ServiceInfo(String beanId, String className, Object service) {
            this.beanId = beanId;
            this.className = className;
            this.service = service;
        }

        public String getBeanId() {
            return beanId;
        }

        public String getClassName() {
            return className;
        }

        public Object getService() {
            return service;
        }

        public AtomicLong getProcessed() {
            return processed;
        }

        public AtomicLong getFailures() {
            return failures;
        }
    }
}
