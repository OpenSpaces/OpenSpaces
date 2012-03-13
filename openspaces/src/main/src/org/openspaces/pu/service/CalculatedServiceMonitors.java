package org.openspaces.pu.service;

import java.util.Map;

/**
 * A base class for service monitors that calculate the service monitors
 * and require caching of the last monitors result. 
 * @author itaif
 * @since 9.0.0
 */
public abstract class CalculatedServiceMonitors extends PlainServiceMonitors {

    protected abstract Map<String,Object> calcMonitors();

    private final Object lock = new Object();
    
    public CalculatedServiceMonitors(String id) {
        super(id);
        super.monitors = null;
    }
    
    @Override
    public Map<String, Object> getMonitors() {
        if (super.getMonitors() == null) {
            synchronized(lock) {
                if (super.getMonitors() == null) {
                    super.monitors = calcMonitors(); 
                }
            }
        }
        return super.getMonitors();
    }
}
