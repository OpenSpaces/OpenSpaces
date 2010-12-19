package org.openspaces.admin.internal.esm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.bean.EnabledBeanConfigCannotBeChangedException;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.machines.ElasticMachineProvisioning;

import com.gigaspaces.grid.gsa.GSProcessOptions;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ProcessingUnitElasticConfig {

    private static final String ELASTIC_SCALE_STRATEGY_CLASSNAMES_KEY = "elastic-scale-strategy-classnames";
    private static final String ELASTIC_MACHINE_ALLOCATOR_CLASSNAMES_KEY = "elastic-machine-allocator-classnames";
    private static final String ELASTIC_SCALE_STRATEGY_ENABLED_CLASSNAME_KEY = "elastic-scale-strategy-enabled-classname";
    private static final String ELASTIC_MACHINE_ALLOCATOR_ENABLED_CLASSNAME_KEY = "elastic-machine-allocator-enabled-classname";

    private static final String ENVIRONMENT_VARIABLES_KEY = "container.environmentVariables.";
    private static final HashMap<String, String> ENVIRONMENT_VARIABLES_DEFAULT = new HashMap<String, String>();
    private static final String OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY = "container.override-jvm-input-arguments";
    private static final boolean OVERRIDE_COMMAND_LINE_ARGUMENTS_DEFAULT = false;
    private static final String COMMAND_LINE_ARGUMENTS_KEY = "container.input-arguments.";
    private static final String[] COMMAND_LINE_ARGUMENTS_DEFAULT = new String[] {};
    private static final String USE_SCRIPT_KEY = "container.use-script";
    private static final boolean USE_SCRIPT_DEFAULT = false;

    private final StringProperties properties;

    public ProcessingUnitElasticConfig(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public Map<String, String> getProperties() {
        return this.properties.getProperties();
    }

    private BeanConfigPropertiesManager toElasticScaleStrategyPropertiesManager() {
        return new DynamicProcessingUnitBeanConfigPropertiesManager(ELASTIC_SCALE_STRATEGY_CLASSNAMES_KEY,
                ELASTIC_SCALE_STRATEGY_ENABLED_CLASSNAME_KEY);
    }

    private BeanConfigPropertiesManager toElasticMachineAllocatorPropertiesManager() {
        return new DynamicProcessingUnitBeanConfigPropertiesManager(ELASTIC_MACHINE_ALLOCATOR_CLASSNAMES_KEY,
                ELASTIC_MACHINE_ALLOCATOR_ENABLED_CLASSNAME_KEY);
    }

    public GridServiceContainerConfig getGridServiceContainerConfig() {
        return new GridServiceContainerConfig();
    }

    public class GridServiceContainerConfig {

        public boolean getUseScript() {
            return ProcessingUnitElasticConfig.this.properties.getBoolean(USE_SCRIPT_KEY, USE_SCRIPT_DEFAULT);
        }

        /**
         * Will cause the {@link org.openspaces.admin.gsc.GridServiceContainer} to be started using
         * a script and not a pure Java process.
         */
        public void setUseScript(boolean useScript) {
            ProcessingUnitElasticConfig.this.properties.putBoolean(USE_SCRIPT_KEY, useScript);
        }

        public String[] getCommandLineArguments() {
            return ProcessingUnitElasticConfig.this.properties.getArgumentsArray(COMMAND_LINE_ARGUMENTS_KEY,
                    COMMAND_LINE_ARGUMENTS_DEFAULT);
        }

        /**
         * Will add a JVM level argument when the process is executed using pure JVM. For example,
         * the memory can be controlled using <code>-Xmx512m</code>.
         */
        public void setCommandLineArguments(String[] inputArguments) {
            ProcessingUnitElasticConfig.this.properties.putArgumentsArray(COMMAND_LINE_ARGUMENTS_KEY, inputArguments);
        }

        public Map<String, String> getEnvironmentVariables() {
            return ProcessingUnitElasticConfig.this.properties.getMap(ENVIRONMENT_VARIABLES_KEY,
                    ENVIRONMENT_VARIABLES_DEFAULT);
        }

        /**
         * Sets an environment variable that will be passed to forked process.
         */
        public void setEnvironmentVariables(Map<String, String> environmentVariables) {
            ProcessingUnitElasticConfig.this.properties.putMap(ENVIRONMENT_VARIABLES_KEY, environmentVariables);
        }

        public boolean getOverrideCommandLineArguments() {
            return ProcessingUnitElasticConfig.this.properties.getBoolean(OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY,
                    OVERRIDE_COMMAND_LINE_ARGUMENTS_DEFAULT);
        }

        /**
         * Will cause JVM options added using {@link #setCommandLineArguments(String)} to override
         * all the vm arguments that the JVM will start by default with.
         */
        public void setOverrideCommandLineArguments(boolean overrideCommandLineArguments) {
            ProcessingUnitElasticConfig.this.properties.putBoolean(OVERRIDE_COMMAND_LINE_ARGUMENTS_KEY,
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
    }

    class DynamicProcessingUnitBeanConfigPropertiesManager implements BeanConfigPropertiesManager {

        private final String enabledClassnameKey;
        private final String classnamesKey;

        public DynamicProcessingUnitBeanConfigPropertiesManager(String classnamesKey, String enabledClassnameKey) {
            this.enabledClassnameKey = enabledClassnameKey;
            this.classnamesKey = classnamesKey;
        }

        public void putConfig(String beanClassName, Map<String, String> properties) throws BeanConfigNotFoundException {
            
            if (isBeanEnabled(beanClassName)) {
                throw new EnabledBeanConfigCannotBeChangedException("Cannot modify bean [" + beanClassName + "] configuration while it is enabled. Disable it first.");
            }
            
            ProcessingUnitElasticConfig.this.properties.putMap(beanClassName + ".", properties);
            addBeanInternal(beanClassName);
        }

       public void enableBean(String beanClassName) throws BeanConfigNotFoundException {
            if (!containsBeanInternal(beanClassName)) {
                throw new BeanConfigNotFoundException("Failed to enable bean [" + beanClassName + "] since it does not exist.");
            }
            ProcessingUnitElasticConfig.this.properties.putArray(enabledClassnameKey,
                    new String[] { beanClassName }, ",");
        }

        public void disableBean(String beanClassName) throws BeanConfigNotFoundException {

            if (!containsBeanInternal(beanClassName)) {
                throw new BeanConfigNotFoundException("Failed to disable bean [" + beanClassName + "] since it does not exist.");
            }
            
            List<String> enabled = Arrays.asList(getEnabledBeansClassNames());
            
            if (enabled.contains(beanClassName)) {
                enabled.remove(beanClassName);
                ProcessingUnitElasticConfig.this.properties.putArray(enabledClassnameKey, enabled.toArray(new String[]{}) , ",");
            }
        }

        public boolean removeConfig(String beanClassName) {
            
            if (isBeanEnabled(beanClassName)) {
                throw new EnabledBeanConfigCannotBeChangedException("Cannot remove configuration of beanClassName " + beanClassName + " since it is enabled. disable it first.");
            }
            
            boolean removed = false;
            if (containsBeanInternal(beanClassName)) {
                removed = removeBeanInternal(beanClassName);
            }
            return removed;
        }
        
        public boolean isBeanEnabled(String beanClassName) {
            return Arrays.asList(getEnabledBeansClassNames()).contains(beanClassName);
        }
        
        public String[] getEnabledBeansClassNames() {
            return ProcessingUnitElasticConfig.this.properties.getArray(enabledClassnameKey, ",", new String[] {});
        }

        public Map<String, String> getConfig(String beanClassName) throws BeanConfigNotFoundException {
            
            if (!containsBeanInternal(beanClassName)) {
                throw new BeanConfigNotFoundException("Failed to get bean [" + beanClassName + "] since it does not exist.");
            }
            return ProcessingUnitElasticConfig.this.properties.getMap(beanClassName, new HashMap<String, String>());
        }

        public String[] getBeansClassNames() {
            return ProcessingUnitElasticConfig.this.properties.getArray(classnamesKey, ",", new String[] {});
        }
        
        public void disableAllBeans() {
            ProcessingUnitElasticConfig.this.properties.remove(enabledClassnameKey);
        }

        private void addBeanInternal(String beanClassName) {
            List<String> beanList = Arrays.asList(getBeansClassNames());
            if (!beanList.contains(beanClassName)) {
                beanList.add(beanClassName);
                ProcessingUnitElasticConfig.this.properties.putArray(
                        ELASTIC_MACHINE_ALLOCATOR_ENABLED_CLASSNAME_KEY, beanList.toArray(new String[] {}), ",");
            }
        }

        private boolean removeBeanInternal(String beanClassName) {
            boolean removed = false;
            List<String> beanList = Arrays.asList(getBeansClassNames());
            if (beanList.contains(beanClassName)) {
                beanList.remove(beanClassName);
                ProcessingUnitElasticConfig.this.properties.putArray(
                        ELASTIC_MACHINE_ALLOCATOR_ENABLED_CLASSNAME_KEY, beanList.toArray(new String[] {}), ",");
                removed = true;
            }
            return removed;
        }

        private boolean containsBeanInternal(String beanClassName) {
            return Arrays.asList(getEnabledBeansClassNames()).contains(beanClassName);
        }
    }

    public boolean isElasticProcessingUnit() {
        return true;
    }

    /**
     * Sets the elastic machine provisioning 
     * @param config - the machine provisioning bean configuration, or null to disable it.
     * @see ElasticMachineProvisioning
     */
    public void setMachineProvisioning(BeanConfig config) {
        BeanConfigPropertiesManager propertiesManager = toElasticMachineAllocatorPropertiesManager();
        propertiesManager.disableAllBeans();
        if (config != null) {
            propertiesManager.putConfig(config.getBeanClassName(), config.getProperties());
            propertiesManager.enableBean(config.getBeanClassName());
        }
    }

    /**
     * Sets the elastic scale strategy 
     * @param config - the scale strategy bean configuration, or null to disable it.
     * @see ProcessingUnit#scale(org.openspaces.admin.pu.ElasticScaleStrategyConfig)
     */
    public void setScaleStrategy(BeanConfig config) {
        BeanConfigPropertiesManager propertiesManager = toElasticScaleStrategyPropertiesManager();
        propertiesManager.disableAllBeans();
        if (config != null) {
            propertiesManager.putConfig(config.getBeanClassName(), config.getProperties());
            propertiesManager.enableBean(config.getBeanClassName());
        }
    }
    
    /**
     * @return the current scale strategy or null if no scale strategy
     */
    public BeanConfig getScaleStrategy() {
        return getEnabledBeanConfig(toElasticScaleStrategyPropertiesManager());
    }

    /**
     * @return the current machine provisioning or null if no machine provisioning.
     */    
    public BeanConfig getMachineProvisioning() {
        return getEnabledBeanConfig(toElasticMachineAllocatorPropertiesManager());
    }

    private BeanConfig getEnabledBeanConfig(final BeanConfigPropertiesManager propertiesManager) {
        BeanConfig config = null;
        if (propertiesManager.getEnabledBeansClassNames().length > 0) {
            final String beanClassName = propertiesManager.getEnabledBeansClassNames()[0];
            config = new BeanConfig() {
                 
                public void setProperties(Map<String, String> properties) {
                    propertiesManager.putConfig(beanClassName, properties);
                }
                
                public Map<String, String> getProperties() {
                    return propertiesManager.getConfig(beanClassName);
                }
                
                public String getBeanClassName() {
                    return beanClassName;
                }
            };
        }
        return config;
    }
    
    public boolean equals(Object other) {
        return other instanceof ProcessingUnitElasticConfig &&
               ((ProcessingUnitElasticConfig)other).properties.equals(properties);
    }
}
