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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.core.util.MemoryUnit;
import org.openspaces.core.util.StringProperties;

import com.gigaspaces.grid.gsa.GSProcessOptions;
import com.gigaspaces.grid.gsa.GSProcessRestartOnExit;


public class GridServiceContainerConfig {

    private static final String ENVIRONMENT_VARIABLES_KEY = "container.environmentVariables.";
    private static final HashMap<String, String> ENVIRONMENT_VARIABLES_DEFAULT = new HashMap<String, String>();
    private static final String OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY = "container.override-commandline-arguments";
    private static final boolean OVERRIDE_COMMAND_LINE_ARGUMENTS_DEFAULT = false;
    private static final String COMMAND_LINE_ARGUMENTS_KEY = "container.commandline-arguments";
    private static final String[] COMMAND_LINE_ARGUMENTS_DEFAULT = new String[] {};
    private static final String USE_SCRIPT_KEY = "container.use-script";
    private static final boolean USE_SCRIPT_DEFAULT = false;
    private static final String MAXIMUM_MEMORY_CAPACITY_MEGABYTES_KEY = "container.memory-capacity";
    private static final Long MAXIMUM_MEMORY_CAPACITY_MEGABYTES_DEFAULT = 0L;
    private static final String RESTART_ON_EXIT_KEY = "restart-on-exit";
    
    StringProperties properties;
        
    public GridServiceContainerConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    public boolean getUseScript() {
        return properties.getBoolean(USE_SCRIPT_KEY, USE_SCRIPT_DEFAULT);
    }

    /**
     * Will cause the {@link org.openspaces.admin.gsc.GridServiceContainer} to be started using
     * a script and not a pure Java process.
     */
    public void setUseScript(boolean useScript) {
        properties.putBoolean(USE_SCRIPT_KEY, useScript);
    }

    public String[] getCommandLineArguments() {
        return properties.getArgumentsArray(COMMAND_LINE_ARGUMENTS_KEY,
                COMMAND_LINE_ARGUMENTS_DEFAULT);
    }

    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example,
     * the memory can be controlled using <code>-Xmx512m</code>.
     */
    public void setCommandLineArguments(String[] commandLineArguments) {
        properties.putArgumentsArray(COMMAND_LINE_ARGUMENTS_KEY, commandLineArguments);
    }

    public Map<String, String> getEnvironmentVariables() {
        return properties.getMap(ENVIRONMENT_VARIABLES_KEY,
                ENVIRONMENT_VARIABLES_DEFAULT);
    }

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        properties.putMap(ENVIRONMENT_VARIABLES_KEY, environmentVariables);
    }

    public boolean getOverrideCommandLineArguments() {
        return properties.getBoolean(OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY,
                OVERRIDE_COMMAND_LINE_ARGUMENTS_DEFAULT);
    }

    /**
     * Will cause JVM options added using {@link #setCommandLineArguments(String)} to override
     * all the vm arguments that the JVM will start by default with.
     */
    public void setOverrideCommandLineArguments(boolean overrideCommandLineArguments) {
        properties.putBoolean(OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY,
                overrideCommandLineArguments);
    }

    /**
     * @return the Xmx settings if available without the prefix. For example "1024m" or 0 if not
     *         found.
     */
    public long getMaximumJavaHeapSizeInMB() {
        long memoryInMB = MAXIMUM_MEMORY_CAPACITY_MEGABYTES_DEFAULT;

        String memory = getCommandLineArgumentIfExists("-Xmx");
        if (memory != null) {
            memoryInMB = MemoryUnit.toMegaBytes(memory);
        }
        return memoryInMB;
    }


    /**
     * @return the Xmx settings if available without the prefix. For example "1024m" or 0 if not
     *         found.
     */
    public long getMinimumJavaHeapSizeInMB() {
        long memoryInMB = 0;

        String memory = getCommandLineArgumentIfExists("-Xms");
        if (memory != null) {
            memoryInMB = MemoryUnit.toMegaBytes(memory);
        }
        return memoryInMB;
    }
    
    public void addMaximumJavaHeapSizeInMBCommandLineArgument(long maximumMemoryCapacityInMB) {
        addCommandLineArgument("-Xmx" + maximumMemoryCapacityInMB + MemoryUnit.MEGABYTES.getPostfix());
    }
    
    public void addMinimumJavaHeapSizeInMBCommandLineArgument(long minimumMemoryCapacityInMB) {
        addCommandLineArgument("-Xms" + minimumMemoryCapacityInMB + MemoryUnit.MEGABYTES.getPostfix());
    }
    
    public long getMaximumMemoryCapacityInMB() {
        return properties.getLong(MAXIMUM_MEMORY_CAPACITY_MEGABYTES_KEY, MAXIMUM_MEMORY_CAPACITY_MEGABYTES_DEFAULT);
    }

    /**
     * Sets the total expected memory being used by this process and any subprocesses it forks
     * (includes java heap, non-java heap, forked processes, etc...) or 0 if undefined
     */
    public void setMaximumMemoryCapacityInMB(long memoryInMB) {
        properties.putLong(MAXIMUM_MEMORY_CAPACITY_MEGABYTES_KEY, memoryInMB);
    }
    
    /**
     * @return the com.gs.zones value if available or null if not found.
     */
    public String[] getZones() {
        return getEnvironmentVariableIfExists("com.gs.zones").split(",");
    }

    /**
     * @param name
     *            - the environment variable exact name
     * @return the value of the variable if it is in {@link #getEnvironmentVariables()}, or if
     *         "-Dname=value" is in {@link #getCommandLineArguments()}
     */
    private String getEnvironmentVariableIfExists(String name) {
        String value = getCommandLineArgumentIfExists("-D" + name + "=");
        if (value == null) {
            value = getEnvironmentVariables().get(name);
        }
        return value;
    }

    /**
     * @param prefix
     *            - the input argument prefix, for example "-Xmx"
     * @return the argument value without the prefix or null if it is not in
     *         {@link #getCommandLineArguments()}
     */
    private String getCommandLineArgumentIfExists(String prefix) {
        String requiredArg = null;
        for (final String arg : this.getCommandLineArguments()) {
            if (arg.startsWith(prefix)) {
                requiredArg = arg;
            }
        }
        if (requiredArg != null) {
            requiredArg = requiredArg.substring(prefix.length());
        }
        return requiredArg;
    }

    public void addCommandLineArgument(String argument) {
        List<String> arguments = new ArrayList<String>();
        arguments.addAll(Arrays.asList(this.getCommandLineArguments()));
        arguments.add(argument);
        this.setCommandLineArguments(arguments.toArray(new String[] {}));
    }

    public void setEnvironmentVariable(String name, String value) {
        Map<String, String> environmentVariables = this.getEnvironmentVariables();
        environmentVariables.put(name, value);
        this.setEnvironmentVariables(environmentVariables);
    }


    public GSProcessRestartOnExit getRestartOnExit() {
        
        GSProcessRestartOnExit restartOnExit = null;
        String value = properties.get(RESTART_ON_EXIT_KEY);
        if (value != null) {
            restartOnExit = GSProcessRestartOnExit.valueOf(value);
        }
        return restartOnExit;
    }
    
    public void setRestartOnExit(GSProcessRestartOnExit restartOnExit) {
        properties.put(RESTART_ON_EXIT_KEY, restartOnExit.toString());
    }
    
    public GSProcessOptions getOptions() {
        
        GSProcessOptions options = new GSProcessOptions("gsc");
        options.setUseScript(getUseScript());
        if (getOverrideCommandLineArguments()) {
            options.setVmInputArguments(getCommandLineArguments());
        } else {
            options.setVmAppendableInputArguments(getCommandLineArguments());
        }
        options.setEnvironmentVariables(getEnvironmentVariables());
        options.setRestartOnExit(getRestartOnExit());
        return options;
    }
    
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    
}
