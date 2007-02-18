package org.openspaces.event.adapter;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class DeafultEventListenerAdapter extends AbstractReflectionEventListenerAdapter {

    public static final String DEFAULT_LISTENER_METHOD_NAME = "handleEvent";

    private String methodName = DEFAULT_LISTENER_METHOD_NAME;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

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
