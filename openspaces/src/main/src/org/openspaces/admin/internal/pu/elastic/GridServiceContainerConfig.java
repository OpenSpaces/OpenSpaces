package org.openspaces.admin.internal.pu.elastic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.core.util.MemoryUnit;
import org.openspaces.core.util.StringProperties;

import com.gigaspaces.grid.gsa.GSProcessOptions;


public class GridServiceContainerConfig {

    private static final String ENVIRONMENT_VARIABLES_KEY = "container.environmentVariables.";
    private static final HashMap<String, String> ENVIRONMENT_VARIABLES_DEFAULT = new HashMap<String, String>();
    private static final String OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY = "container.override-jvm-input-arguments";
    private static final boolean OVERRIDE_COMMAND_LINE_ARGUMENTS_DEFAULT = false;
    private static final String COMMAND_LINE_ARGUMENTS_KEY = "container.input-arguments.";
    private static final String[] COMMAND_LINE_ARGUMENTS_DEFAULT = new String[] {};
    private static final String USE_SCRIPT_KEY = "container.use-script";
    private static final boolean USE_SCRIPT_DEFAULT = false;

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
        long memoryInMB = 0;

        String memory = getCommandLineArgumentIfExists("-Xmx");
        if (memory != null) {
            memoryInMB = MemoryUnit.toMegaBytes(memory);
        }
        return memoryInMB;
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
        List<String> arguments = Arrays.asList(this.getCommandLineArguments());
        arguments.add(argument);
        this.setCommandLineArguments(arguments.toArray(new String[] {}));
    }

    public void setEnvironmentVariable(String name, String value) {
        Map<String, String> environmentVariables = this.getEnvironmentVariables();
        environmentVariables.put(name, value);
        this.setEnvironmentVariables(environmentVariables);
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
        return options;
    }
    
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }
    
}
