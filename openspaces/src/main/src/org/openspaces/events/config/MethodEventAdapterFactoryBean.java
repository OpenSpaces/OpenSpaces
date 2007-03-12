package org.openspaces.events.config;

import org.openspaces.events.adapter.AbstractResultEventListenerAdapter;
import org.openspaces.events.adapter.MethodEventListenerAdapter;

/**
 * @author kimchy
 */
public class MethodEventAdapterFactoryBean extends AbstractResultEventAdapterFactoryBean {

    private Object delegate;

    private String methodName;

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    protected AbstractResultEventListenerAdapter createAdapter() {
        MethodEventListenerAdapter adapter = new MethodEventListenerAdapter();
        adapter.setDelegate(delegate);
        adapter.setMethodName(methodName);
        return adapter;
    }
}
