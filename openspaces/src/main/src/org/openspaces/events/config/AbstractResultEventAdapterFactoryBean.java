package org.openspaces.events.config;

import org.openspaces.events.adapter.AbstractResultEventListenerAdapter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author kimchy
 */
public abstract class AbstractResultEventAdapterFactoryBean implements FactoryBean, InitializingBean {

    private Long writeLease;

    private Boolean updateOrWrite;

    private Long updateTimeout;


    private AbstractResultEventListenerAdapter adapter;

    
    public void setWriteLease(Long writeLease) {
        this.writeLease = writeLease;
    }

    public void setUpdateOrWrite(Boolean updateOrWrite) {
        this.updateOrWrite = updateOrWrite;
    }

    public void setUpdateTimeout(Long updateTimeout) {
        this.updateTimeout = updateTimeout;
    }
    

    public void afterPropertiesSet() throws Exception {
        adapter = createAdapter();
        if (writeLease != null) {
            adapter.setWriteLease(writeLease);
        }
        if (updateOrWrite) {
            adapter.setUpdateOrWrite(updateOrWrite);
        }
        if (updateTimeout != null) {
            adapter.setUpdateTimeout(updateTimeout);
        }
        if (adapter instanceof InitializingBean) {
            ((InitializingBean) adapter).afterPropertiesSet();
        }
    }

    protected abstract AbstractResultEventListenerAdapter createAdapter();

    public Object getObject() throws Exception {
        return this.adapter;
    }

    public Class getObjectType() {
        return this.adapter.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
