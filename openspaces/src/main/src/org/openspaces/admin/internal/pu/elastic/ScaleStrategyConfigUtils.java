/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.pu.elastic;

import org.openspaces.core.util.StringProperties;

public class ScaleStrategyConfigUtils {

    private static final String MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY = "max-number-of-containers-per-machine";
    private static final int MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT = Integer.MAX_VALUE;
    private static final String MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY = "min-number-of-containers-per-machine";
    private static final int MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT = 0;
    private static final String MAX_NUMBER_OF_CONTAINERS_KEY = "max-number-of-containers";
    private static final String MIN_NUMBER_OF_CONTAINERS_KEY = "min-number-of-containers";
    private static final int MAX_NUMBER_OF_CONTAINERS_DEFAULT = Integer.MAX_VALUE;
    private static final int MIN_NUMBER_OF_CONTAINERS_DEFAULT = 0;
    private static final int POLLING_INTERVAL_SECONDS_DEFAULT = 1;
    private static final String POLLING_INTERVAL_SECONDS_KEY = "polling-interval-seconds";
    private static final String MAX_NUMBER_OF_CONCURRENT_RELOCATIONS_PER_MACHINE_KEY = "max-number-of-concurrent-relocations-per-machine";
    private static final int MAX_NUMBER_OF_CONCURRENT_RELOCATIONS_PER_MACHINE_DEFAULT = 1;
    private static final boolean AT_MOST_ONE_CONTAINER_PER_MACHINE_DEFAULT = false;
    private static final String AT_MOST_ONE_CONTAINER_PER_MACHINE_KEY = "at-most-one-container-per-machine";
    
    public static void setMaxNumberOfContainersPerMachine(
            StringProperties properties,
            int maxNumberOfContainersPerMachine) {
        
        properties.putInteger(MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, maxNumberOfContainersPerMachine);
    }

    public static int getMaxNumberOfContainersPerMachine(
            StringProperties properties) {
        
        return properties.getInteger(MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT);
    }

    public static void setMinNumberOfContainersPerMachine(
            StringProperties properties,
            int minNumberOfContainersPerMachine) {
        
        properties.putInteger(MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, minNumberOfContainersPerMachine);
    }

    public static int getMinNumberOfContainersPerMachine(
            StringProperties properties) {
    
        return properties.getInteger(MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT);
    }
    
    public static void setMaxNumberOfContainers(
            StringProperties properties,
            int maxNumberOfContainers) {
     
        properties.putInteger(MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, maxNumberOfContainers);
    }

    public static int getMaxNumberOfContainers(
            StringProperties properties) {
     
        return properties.getInteger(MAX_NUMBER_OF_CONTAINERS_KEY, MAX_NUMBER_OF_CONTAINERS_DEFAULT);
    }

    public static void setMinNumberOfContainers(
            StringProperties properties,
            int minNumberOfContainers) {
        
        properties.putInteger(MIN_NUMBER_OF_CONTAINERS_KEY, minNumberOfContainers);
    }

    public static int getMinNumberOfContainers(
            StringProperties properties) {
        
        return properties.getInteger(MIN_NUMBER_OF_CONTAINERS_KEY, MIN_NUMBER_OF_CONTAINERS_DEFAULT);
    }

    public static int getPollingIntervalSeconds(StringProperties properties) {
        return properties.getInteger(POLLING_INTERVAL_SECONDS_KEY, POLLING_INTERVAL_SECONDS_DEFAULT);
    }

    public static void setPollingIntervalSeconds(StringProperties properties, int value) {
        properties.putInteger(POLLING_INTERVAL_SECONDS_KEY, value);
    }

    public static int getMaxConcurrentRelocationsPerMachine(StringProperties properties) {
        return properties.getInteger(MAX_NUMBER_OF_CONCURRENT_RELOCATIONS_PER_MACHINE_KEY, MAX_NUMBER_OF_CONCURRENT_RELOCATIONS_PER_MACHINE_DEFAULT);
    }
    
    public static void setMaxConcurrentRelocationsPerMachine(StringProperties properties, int maxNumberOfConcurrentRelocationsPerMachine) {
        if (maxNumberOfConcurrentRelocationsPerMachine <= 0) {
            throw new IllegalArgumentException("maxNumberOfConcurrentRelocationsPerMachine must be 1 or higher.");
        }
        properties.putInteger(MAX_NUMBER_OF_CONCURRENT_RELOCATIONS_PER_MACHINE_KEY,maxNumberOfConcurrentRelocationsPerMachine);        
    }

    public static void setAtMostOneContainerPerMachine(StringProperties properties, boolean isSingleContainersPerMachine) {
        properties.putBoolean(AT_MOST_ONE_CONTAINER_PER_MACHINE_KEY,isSingleContainersPerMachine);
    }
    
    public static boolean isSingleContainerPerMachine(StringProperties properties) {
        return properties.getBoolean(AT_MOST_ONE_CONTAINER_PER_MACHINE_KEY,AT_MOST_ONE_CONTAINER_PER_MACHINE_DEFAULT);
    }

}
