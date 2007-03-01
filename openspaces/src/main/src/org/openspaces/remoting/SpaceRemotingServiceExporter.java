package org.openspaces.remoting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventTemplateProvider;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>Exports a list of services (beans) as remote services with the Space as the transport
 * layer. All the interfaces each service implements are regsitered as lookup names (matching
 * {@link SpaceRemoteInvocation#getLookupName()} which are then used to lookup the actual
 * service when a remote invocation is received. The correct service and its method are then
 * executed and a {@link org.openspaces.remoting.SpaceRemoteResult} is written back to the
 * space. The remote result can either hold the return value (or <code>null</code> in case of
 * void return value) or an exception that was thrown by the service.
 *
 * <p>The exported implements {@link org.openspaces.events.SpaceDataEventListener} which means
 * that it acts as a listener to data events and should be used with the differnet event containers
 * such as {@link org.openspaces.events.polling.SimplePollingEventListenerContainer}.
 *
 * <p>It also implements {@link org.openspaces.events.EventTemplateProvider} which means that within
 * the event container configuration there is no need to configure the template, as it uses the
 * one provided by this exported.
 *
 * @author kimchy
 * @see org.openspaces.events.polling.SimplePollingEventListenerContainer
 * @see org.openspaces.remoting.SpaceRemoteInvocation
 * @see org.openspaces.remoting.SpaceRemoteResult
 * @see org.openspaces.remoting.SpaceRemotingProxyFactoryBean
 */
public class SpaceRemotingServiceExporter implements SpaceDataEventListener, InitializingBean, ApplicationContextAware, EventTemplateProvider {

    private static final Log logger = LogFactory.getLog(SpaceRemotingServiceExporter.class);

    private List services;


    private ApplicationContext applicationContext;

    private Map interfaceToService = new HashMap();

    public void setServices(List services) {
        this.services = services;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(services, "services property is required");
        // go over the services and create the inteface to service lookup
        for (Iterator it = services.iterator(); it.hasNext();) {
            Object service = it.next();
            Class[] interfaces = ClassUtils.getAllInterfaces(service);
            for (int j = 0; j < interfaces.length; j++) {
                interfaceToService.put(interfaces[j].getName(), service);
            }
        }
    }

    public Object getTemplate() {
        return new SpaceRemoteInvocation();
    }

    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        SpaceRemoteInvocation remoteInvocation = (SpaceRemoteInvocation) data;

        Object service = interfaceToService.get(remoteInvocation.lookupName);
        if (service == null) {
            // we did not get an interface, maybe it is a bean name?
            service = applicationContext.getBean(remoteInvocation.lookupName);
            if (service == null) {
                writeResponse(gigaSpace, remoteInvocation, new ServiceNotFoundSpaceRemotingException(remoteInvocation.getLookupName()));
                return;
            }
        }

        // TODO could so some caching of the method invoker for better performance
        Object[] arguments = remoteInvocation.getArguments();
        if (arguments == null) {
            arguments = new Object[0];
        }
        Class[] argumentTypes = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argumentTypes[i] = (arguments[i] != null ? arguments[i].getClass() : Object.class);
        }

        Method method;
        try {
            method = service.getClass().getMethod(remoteInvocation.getMethodName(), argumentTypes);
        } catch (Exception e) {
            writeResponse(gigaSpace, remoteInvocation, new ServiceMethodNotFoundSpaceRemotingException(remoteInvocation.getMethodName(), e));
            return;
        }
        try {
            Object retVal = method.invoke(service, arguments);
            writeResponse(gigaSpace, remoteInvocation, retVal);
        } catch (InvocationTargetException e) {
            writeResponse(gigaSpace, remoteInvocation, e.getTargetException());
        } catch (IllegalAccessException e) {
            writeResponse(gigaSpace, remoteInvocation, new ServiceMethodNotFoundSpaceRemotingException(remoteInvocation.getMethodName(), e));
        }
    }

    private void writeResponse(GigaSpace gigaSpace, SpaceRemoteInvocation remoteInvocation, SpaceRemotingException e) {
        if (remoteInvocation.oneWay == null || !remoteInvocation.oneWay.booleanValue()) {
            gigaSpace.write(new SpaceRemoteResult(remoteInvocation, e));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Remoting execution is configured as one way and an exception was thrown", e);
            }
        }
    }

    private void writeResponse(GigaSpace gigaSpace, SpaceRemoteInvocation remoteInvocation, Object retVal) {
        if (remoteInvocation.oneWay == null || !remoteInvocation.oneWay.booleanValue()) {
            gigaSpace.write(new SpaceRemoteResult(remoteInvocation, retVal));
        }
    }
}
