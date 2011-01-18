package org.openspaces.admin.pu.elastic.config;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
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
        implements BeanConfig {

    private static final String STRATEGY_NAME = "scale-strategy.eager.";
 
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
    
    public int getReservedMemoryCapacityPerMachineInMB() {
        return ScaleStrategyConfigUtils.getReservedMemoryCapacityPerMachineInMB(properties); 
    }
    
    public void setReservedMemoryCapacityPerMachineInMB(int reservedInMB) {
        ScaleStrategyConfigUtils.setReservedMemoryCapacityPerMachineInMB(properties, reservedInMB); 
    }
    
    public int getMaximumNumberOfConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaximumNumberOfConcurrentRelocationsPerMachine(properties);
    }
    
    public void setMaximumNumberOfConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaximumNumberOfConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    public String getBeanClassName() {
        return EagerScaleStrategyBean.class.getName();
    }


}



