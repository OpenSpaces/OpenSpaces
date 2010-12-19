package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.internal.pu.elastic.config.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.elastic.MemoryCapacityScaleStrategyBean;

/**
 * Defines an automatic scaling strategy that monitors each container's memory usage percentage.
 * The container's memory usage percentage is averaged over the specified sliding time window.
 * 
 * When the average usage percentage is above the high threshold more containers are started.
 * When the average usage percentage is below the low threshold containers are killed.
 *
 * @see MemoryCapacityScaleConfigurer
 * @author itaif
 */
public class MemoryCapacityScaleConfig 
    implements  MinNumberOfContainersScaleConfig,  
                MaxNumberOfContainersScaleConfig ,
                MinNumberOfContainersPerMachineScaleConfig,
                MaxNumberOfContainersPerMachineScaleConfig,
                BeanConfig {

    private static final String SLIDING_WINDOW_MILLISECONDS_KEY = "sliding-window-milliseconds";
    private static final long SLIDING_WINDOW_MILLISECONDS_DEFAULT = 60000;
    private static final String SCALE_OUT_USAGE_PERCENTAGE_KEY = "scale-out-usage-percentage";
    private static final int SCALE_OUT_USAGE_PERCENTAGE_DEFAULT = 70;
    private static final String SCALE_IN_USAGE_PERCENTAGE_KEY = "scale-in-usage-percentage";
    private static final int SCALE_IN_USAGE_PERCENTAGE_DEFAULT = 20;
    
    private StringProperties properties;
    
    public MemoryCapacityScaleConfig() {
        properties = new StringProperties();
    }
    
    /**
     * Defines the averaging period in milliseconds of the sliding time window.  
     */
    public void setSlidingTimeWindowMilliseconds(long milliseconds) {
        properties.putLong(SLIDING_WINDOW_MILLISECONDS_KEY, milliseconds);
    }

    public long getSlidingTimeWindowMilliseconds() {
        return properties.getLong(SLIDING_WINDOW_MILLISECONDS_KEY, SLIDING_WINDOW_MILLISECONDS_DEFAULT);
    }

    /**
     * Defines the high threshold above which scale out is triggered.  
     */
    public void setScaleOutWhenAverageAbove(int usagePercentage) {
        properties.putInteger(SCALE_OUT_USAGE_PERCENTAGE_KEY, usagePercentage);
    }
    
    public int getScaleOutWhenAverageAbove() {
        return properties.getInteger(SCALE_OUT_USAGE_PERCENTAGE_KEY, SCALE_OUT_USAGE_PERCENTAGE_DEFAULT);
    }
    
    /**
     * Defines the low threshold below which scale in is triggered.  
     */
    public void setScaleInWhenAverageBelow(int usagePercentage) {
        properties.putInteger(SCALE_IN_USAGE_PERCENTAGE_KEY, usagePercentage);
    }
    
    public int getScaleInWhenAverageBelow() {
        return properties.getInteger(SCALE_IN_USAGE_PERCENTAGE_KEY, SCALE_IN_USAGE_PERCENTAGE_DEFAULT);
    }

    public void setMaxNumberOfContainers(int numberOfContainers) {
        ScaleStrategyConfigUtils.setMaxNumberOfContainers(properties, numberOfContainers);
    }
    
    public int getMaxNumberOfContainers() {
        return ScaleStrategyConfigUtils.getMaxNumberOfContainers(properties);
    }

    public void setMaxNumberOfContainersPerMachine(int numberOfContainers) {
        ScaleStrategyConfigUtils.setMaxNumberOfContainersPerMachine(properties, numberOfContainers);
    }
    
    public int getMaxNumberOfContainersPerMachine() {
        return ScaleStrategyConfigUtils.getMaxNumberOfContainersPerMachine(properties);
    }

    public int getMinNumberOfContainers() {
        return ScaleStrategyConfigUtils.getMinNumberOfContainers(properties);
    }

    public void setMinNumberOfContainers(int minNumberOfContainers) {
        ScaleStrategyConfigUtils.setMinNumberOfContainers(properties, minNumberOfContainers);
    }

    public int getMinNumberOfContainersPerMachine() {
        return ScaleStrategyConfigUtils.getMinNumberOfContainersPerMachine(properties);
    }

    public void setMinNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        ScaleStrategyConfigUtils.setMinNumberOfContainersPerMachine(properties, minNumberOfContainersPerMachine);
    }

    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }
    
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties, pollingIntervalSeconds);
    }
    
    public Map<String,String> getProperties() {
        return this.properties.getProperties();
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public String getBeanClassName() {
        return MemoryCapacityScaleStrategyBean.class.getName();
    }

    public void applyRecommendedSettings() {
        // TODO Auto-generated method stub
        
    }
}
