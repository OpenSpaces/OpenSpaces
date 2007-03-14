package org.openspaces.events.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ObjectUtils;

/**
 * Base class for reflection driven invocation of event listener methods. Handles cases where a
 * single event listener method is configured, or several method event listeners are configured (all
 * must have the same name). The event listener methods are found and delegated to an object
 * configured using {@link #setDelegate(Object)}.
 * 
 * <p>
 * Subclasses must implement the {@link #doGetListenerMethods()} in order to list all the possible
 * event listener methods. If more than one event listener method is found, all event listener
 * methods must have the same name.
 * 
 * <p>
 * For best performance a single event listener method should be used. If a single event listener is
 * found (by subclasses), caching of the method can be done in order to perform the reflection
 * execution faster. If more than one event listener method is found - dynamic discovery of the
 * appropiate method is done for each listener invocation.
 * 
 * <p>
 * Event listening methods can have no parameters or one or more parameres mapping to
 * {@link SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,org.springframework.transaction.TransactionStatus,Object)}
 * parameters order. If the method has a return value it will be handled thanks to
 * {@link org.openspaces.events.adapter.AbstractResultEventListenerAdapter}.
 * 
 * <p>
 * Having more than one event listening method allows for writing specifc listener methods handling
 * different data event types (usually different types within the same inheritance tree). This
 * allows to remove the need for <code>instnaceof</code> checks within the listener code. If a
 * single listening method is used, it can still have a specific class type for the event data
 * object thanks to Java reflection, though if the event listener will be invoked with a different
 * type a reflection exception will be thrown.
 * 
 * @author kimchy
 */
public abstract class AbstractReflectionEventListenerAdapter extends AbstractResultEventListenerAdapter implements
        InitializingBean {

    /**
     * Logger available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private Object delegate;

    private Method[] listenerMethods;

    /**
     * The event listener delegate that will be searched for event listening methods and will have
     * its method executed.
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the event listener delegate.
     */
    protected Object getDelegate() {
        return this.delegate;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegate, "delegate property is required");
        listenerMethods = doGetListenerMethods();
        if (listenerMethods == null || listenerMethods.length == 0) {
            throw new IllegalArgumentException("No event listening methods found in delegate [" + delegate + "]");
        }
        if (listenerMethods.length > 1) {
            // more than one listener methods are allowed, but they must be with the same name
            // if more than one listener method is defined, execution will be slower
            String methodName = listenerMethods[0].getName();
            for (int i = 1; i < listenerMethods.length; i++) {
                if (!methodName.equals(listenerMethods[i].getName())) {
                    throw new IllegalArgumentException("All listener methods must have the same name. Found ["
                            + methodName + "] and [" + listenerMethods[i].getName() + "]");
                }
            }
            int numbersOfParams = listenerMethods[0].getParameterTypes().length;
            for (int i = 1; i < listenerMethods.length; i++) {
                if (numbersOfParams != listenerMethods[i].getParameterTypes().length) {
                    throw new IllegalArgumentException("All listener methods must have number of parameters. Found ["
                            + numbersOfParams + "] and [" + listenerMethods[i].getParameterTypes().length + "]");
                }
            }
        }
        for (Method listenerMethod : listenerMethods) {
            listenerMethod.setAccessible(true);
        }
    }

    /**
     * Delegates the event listener invocation to the appropiate method of the configured
     * {@link #setDelegate(Object)}. If a single event listener delegate method is found, uses the
     * cached reflection Method. If more than one event listener delegate method is configured uses
     * reflection to dynamically find the relevant event listener method.
     */
    protected Object onEventWithResult(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        Method listenerMethod = listenerMethods[0];
        int numberOfParameters = listenerMethod.getParameterTypes().length;

        // set up the arguments passed to the method
        Object[] listenerArguments = null;
        if (numberOfParameters == 1) {
            listenerArguments = new Object[] { data };
        } else if (numberOfParameters == 2) {
            listenerArguments = new Object[] { data, gigaSpace };
        } else if (numberOfParameters == 3) {
            listenerArguments = new Object[] { data, gigaSpace, txStatus };
        } else if (numberOfParameters == 4) {
            listenerArguments = new Object[] { data, gigaSpace, txStatus, source };
        }

        Object result = null;
        if (listenerMethods.length == 1) {
            // single method, use the already obtained Method to invoke the listener
            try {
                result = listenerMethod.invoke(delegate, listenerArguments);
            } catch (IllegalAccessException ex) {
                throw new PermissionDeniedDataAccessException("Failed to invoke event method ["
                        + listenerMethod.getName() + "]", ex);
            } catch (InvocationTargetException ex) {
                throw new ListenerExecutionFailedException("Listener event method '" + listenerMethod.getName()
                        + "' threw exception", ex.getTargetException());
            }
        } else {
            // more than one method, we need to use reflection to find the matched method
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(getDelegate());
            methodInvoker.setTargetMethod(listenerMethod.getName());
            methodInvoker.setArguments(listenerArguments);
            try {
                methodInvoker.prepare();
                result = methodInvoker.invoke();
            } catch (InvocationTargetException ex) {
                throw new org.springframework.jms.listener.adapter.ListenerExecutionFailedException("Listener method '"
                        + listenerMethod.getName() + "' threw exception", ex.getTargetException());
            } catch (Throwable ex) {
                throw new ListenerExecutionFailedException("Failed to invoke target method '"
                        + listenerMethod.getName() + "' with arguments "
                        + ObjectUtils.nullSafeToString(listenerArguments), ex);
            }

        }
        return result;
    }

    /**
     * Subclasses should implement this in order to provide a list of all the possible event
     * listener delegate methods.
     * 
     * @return A list of all the event listener delegate methods
     * @throws Exception
     */
    protected abstract Method[] doGetListenerMethods() throws Exception;
}
