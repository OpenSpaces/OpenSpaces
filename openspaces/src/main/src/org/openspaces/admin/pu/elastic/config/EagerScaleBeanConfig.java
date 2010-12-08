package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.config.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;

/*
 * Defines an automatic scaling strategy that consumes capacity eagerly.
 * When enabled the processing unit scales out whenever free capacity that meets zone and isolation constraints is available.
 * 
 * @see EagerScaleStrategyConfigurer
 * @author itaif
 */
public class EagerScaleBeanConfig 
        implements MaxNumberOfContainersScaleConfig ,
                   MinNumberOfContainersPerMachineScaleConfig,
                   MaxNumberOfContainersPerMachineScaleConfig,
                   ScaleBeanConfig {

    private static final String STRATEGY_NAME = "scale-strategy.eager.";
 
    private StringProperties properties;
    
    public EagerScaleBeanConfig() {
        this.properties = new StringProperties();
    }
    
    EagerScaleBeanConfig(StringProperties properties) {
        this.properties = properties;
    }
   
    public void setMaxNumberOfContainers(int maxNumberOfContainers) {
        ScaleStrategyConfigUtils.setMaxNumberOfContainers(properties, maxNumberOfContainers);
    }

    public void setMinNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        ScaleStrategyConfigUtils.setMinNumberOfContainersPerMachine(properties, minNumberOfContainersPerMachine);
    }
    
    public void setMaxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        ScaleStrategyConfigUtils.setMaxNumberOfContainersPerMachine(properties, maxNumberOfContainersPerMachine);
    }
    
    public int getMaxNumberOfContainers() {
        return ScaleStrategyConfigUtils.getMaxNumberOfContainers(properties);
    }

    public int getMaxNumberOfContainersPerMachine() {
        return ScaleStrategyConfigUtils.getMaxNumberOfContainersPerMachine(properties);
    }

    public int getMinNumberOfContainersPerMachine() {
        return ScaleStrategyConfigUtils.getMinNumberOfContainersPerMachine(properties);
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
        // TODO Auto-generated method stub
        return null;
    }

    public void applyRecommendedSettings() {
        // TODO Auto-generated method stub
        
    }

}



