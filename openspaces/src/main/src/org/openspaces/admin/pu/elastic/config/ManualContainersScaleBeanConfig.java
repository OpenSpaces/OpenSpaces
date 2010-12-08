package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.config.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;

/*
 * Defines a manual scaling strategy that defines the target number of containers.
 * When enabled the processing unit scales out or scales in to meet the specified number of containers.
 * 
 * @see ManualContainersScaleStrategyConfigurer
 * @author itaif
 */
public class ManualContainersScaleBeanConfig 
    implements MinNumberOfContainersPerMachineScaleConfig,
               MaxNumberOfContainersPerMachineScaleConfig,
               ScaleBeanConfig {

    private static final String STRATEGY_NAME = "scale-strategy.manual-containers.";
    private static final String NUMBER_OF_CONTAINERS_KEY = "number-of-containers";
    private static final int NUMBER_OF_CONTAINERS_DEFAULT = 1;
    private StringProperties properties;
    
    public ManualContainersScaleBeanConfig() {
        this.properties = new StringProperties();
    }
       
    /**
     * Defines the target number of containers for the elastic processing unit
     */
    public void setNumberOfContainers(int numberOfContainers) {
        properties.putInteger(NUMBER_OF_CONTAINERS_KEY, numberOfContainers);
    }

    /**
     * @return the target number of containers for the elastic processing unit
     */
    public int getNumberOfContainers() {
        return properties.getInteger(NUMBER_OF_CONTAINERS_KEY, NUMBER_OF_CONTAINERS_DEFAULT);
    }


    public int getMinNumberOfContainersPerMachine() {
        return ScaleStrategyConfigUtils.getMinNumberOfContainersPerMachine(properties);
    }

    public void setMinNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        ScaleStrategyConfigUtils.setMinNumberOfContainersPerMachine(properties, minNumberOfContainersPerMachine);
        
    }

    public int getMaxNumberOfContainersPerMachine() {
        return ScaleStrategyConfigUtils.getMaxNumberOfContainersPerMachine(properties);
    }

    public void setMaxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        ScaleStrategyConfigUtils.setMaxNumberOfContainersPerMachine(properties, maxNumberOfContainersPerMachine);
        
    }
    
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
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