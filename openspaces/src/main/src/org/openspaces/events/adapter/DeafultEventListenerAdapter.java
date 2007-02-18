package org.openspaces.events.adapter;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The default event listnere adapter allows to configure the method name (using
 * {@link #setMethodName(String)} that event will be delegated to. The default method
 * name is <code>handleEvent</code>.
 *
 * @author kimchy
 */
public class DeafultEventListenerAdapter extends AbstractReflectionEventListenerAdapter {

    /**
     * Default method name to delegate to: <code>handleEvent</code>.
     */
    public static final String DEFAULT_LISTENER_METHOD_NAME = "handleEvent";

    private String methodName = DEFAULT_LISTENER_METHOD_NAME;

    /**
     * Sets the method name the event listener adapter will delegate the events to.
     *
     * @param methodName The method name events will be delegated to
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns a list of all the methods names that match the configured {@link #setMethodName(String)}.
     */
    protected Method[] doGetListenerMethods() throws Exception {
        final List methods = new ArrayList();
        ReflectionUtils.doWithMethods(getDelegate().getClass(),
                new ReflectionUtils.MethodCallback() {
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        methods.add(method);
                    }
                },
                new ReflectionUtils.MethodFilter() {
                    public boolean matches(Method method) {
                        return method.getName().equals(methodName);
                    }
                });
        return (Method[]) methods.toArray(new Method[methods.size()]);
    }
}
