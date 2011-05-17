package org.openspaces.admin.pu.elastic.config;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.EagerScaleStrategyBean;

/*
 * Defines an automatic scaling strategy that consumes capacity eagerly.
 * When enabled the processing unit scales out whenever free capacity that meets zone and isolation constraints is available.
 * 
 * @see EagerScaleStrategyConfigurer
 * @author itaif
 */
public class EagerScaleConfig 
        implements ScaleStrategyConfig , Externalizable {

    private static final long serialVersionUID = 1L;
 
    private StringProperties properties;
    
    public EagerScaleConfig(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    public EagerScaleConfig() {
        this(new HashMap<String,String>());
    }
   
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }
    
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties, pollingIntervalSeconds);
    }
        
    public int getMaxConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaxConcurrentRelocationsPerMachine(properties);
    }
    
    public void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaxConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    public String getBeanClassName() {
        return EagerScaleStrategyBean.class.getName();
    }
    
    public String toString() {
        return this.properties.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        return (other instanceof EagerScaleConfig) &&
                this.properties.equals(((EagerScaleConfig)other).properties);
    }
    
    @Override
    public int hashCode() {
        return this.properties.hashCode();
    }
    

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.properties.getProperties());
        
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.properties = new StringProperties((Map<String,String>)in.readObject());
        
    }
}



