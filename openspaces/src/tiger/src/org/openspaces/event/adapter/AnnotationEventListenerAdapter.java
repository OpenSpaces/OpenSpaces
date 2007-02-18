package org.openspaces.events.adapter;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class AnnotationEventListenerAdapter extends AbstractReflectionEventListenerAdapter {

    protected Method[] doGetListenerMethods() throws Exception {
        final List<Method> methods = new ArrayList<Method>();
        ReflectionUtils.doWithMethods(getDelegate().getClass(),
                new ReflectionUtils.MethodCallback() {
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        methods.add(method);
                    }
                },
                new ReflectionUtils.MethodFilter() {
                    public boolean matches(Method method) {
                        return method.getAnnotation(SpaceDataEvent.class) != null;
                    }
                });
        return methods.toArray(new Method[methods.size()]);
    }
}
