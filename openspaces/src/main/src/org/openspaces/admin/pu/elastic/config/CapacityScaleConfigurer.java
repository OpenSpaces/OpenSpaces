package org.openspaces.admin.pu.elastic.config;

import java.util.concurrent.TimeUnit;

/**
 * Provides fluent API for creating a new {@link CapacityScaleConfig} object.
 * 
 * For example {@code new MemoryCapacityScaleStrategyConfigurer().slidingTimeWindow(60,TimeUnit.SECONDS).scaleOutWhenAboveThreshold(70).getConfig()}
 * 
 * @author itaif
 * 
 */
public class CapacityScaleConfigurer
    implements ScaleStrategyConfigurer<CapacityScaleConfig> {

    private final CapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link CapacityScaleConfig} object.
     * For example {@code new MemoryCapacityScaleStrategyConfigurer().slidingTimeWindow(60,TimeUnit.SECONDS).scaleOutWhenAboveThreshold(70).getConfig()}
     * The default constructor wraps an empty {@link CapacityScaleConfig} object
     */
    public CapacityScaleConfigurer() {
        this.config = new CapacityScaleConfig();
    }
    
    /**
     * @see CapacityScaleConfig#setSlidingTimeWindowMilliseconds(int)
     */
    public CapacityScaleConfigurer slidingTimeWindow(int duration, TimeUnit unit) {
        config.setSlidingTimeWindowMilliseconds(unit.toMillis(duration));
        return this;
    }

    /**
     * @see CapacityScaleConfig#setScaleInWhenAverageAbove(int)
     */
    public CapacityScaleConfigurer scaleInWhenAverageBelow(int usagePercentage) {
        validatePercentage(usagePercentage);
        config.setScaleInWhenAverageBelow(usagePercentage);
        return this;
    }

    /**
     * @see CapacityScaleConfig#setScaleOutWhenAverageAbove(int)
     */
    public CapacityScaleConfigurer scaleOutWhenAverageAbove(int usagePercentage) {
        validatePercentage(usagePercentage);
        config.setScaleOutWhenAverageAbove(usagePercentage);
        return this;
    }

    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public CapacityScaleConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public CapacityScaleConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        config.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public CapacityScaleConfigurer maxNumberOfContainers(int maxNumberOfContainers) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersScaleConfig#setMinNumberOfContainers(int)
     */
    public CapacityScaleConfigurer minNumberOfContainers(int minNumberOfContainers) {
        config.setMinNumberOfContainers(minNumberOfContainers);
        return this;
    }

    /**
     * @see ScaleStrategyConfigurer#getConfig()
     */
    public CapacityScaleConfig getConfig() {
        return config;
    }

    private void validatePercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage must be between 0 and 100. The value " + percentage + " is illegal.");
        }
    }
}
