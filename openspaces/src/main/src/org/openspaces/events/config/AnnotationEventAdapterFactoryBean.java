package org.openspaces.events.config;

import org.openspaces.events.adapter.AbstractResultEventListenerAdapter;
import org.openspaces.events.adapter.AnnotationEventListenerAdapter;

/**
 * @author kimchy
 */
public class AnnotationEventAdapterFactoryBean extends AbstractResultEventAdapterFactoryBean {

    private Object delegate;

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    protected AbstractResultEventListenerAdapter createAdapter() {
        AnnotationEventListenerAdapter adapter = new AnnotationEventListenerAdapter();
        adapter.setDelegate(delegate);
        return adapter;
    }
}
