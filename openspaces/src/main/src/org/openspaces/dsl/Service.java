package org.openspaces.dsl;

import groovy.lang.GroovyShell;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.dsl.ui.UserInterface;

public class Service implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private String icon;

    private String imageTemplate;
    private String defaultScalingUnit;
    private String pidFile;
    private boolean supportsScaling;

    private ServiceLifecycle lifecycle;
    private UserInterface userInterface;
   
    private List<PluginDescriptor> plugins;
    
    private int numInstances = 1;
    private long maxJarSize = 150 * 1024 * 1024; //in bytes
    private boolean keepFile = false;
    
    private Map<String, String> customProperties = new HashMap<String, String>();
    
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // TODO - Remove this
    public static void main(final String[] args) throws Exception {

        /*
         * final BufferedReader reader = new BufferedReader(new FileReader("test-service.groovy"));
         * final StringBuilder sb = new StringBuilder(); while (true) { final String line =
         * reader.readLine(); if (line == null) { break; } else { sb.append(line).append("\n"); } }
         * reader.close();
         * 
         * final String exp = sb.toString();
         */

        // ((GroovyObject) new
        // GroovyClassLoader().parseClass(exp).newInstance()).run();
        // GroovyEngine engine = new GroovyEngine();

        final GroovyShell gs = new GroovyShell();
        final Object obj = gs.evaluate(new File("test-service.groovy"));
        // final Object obj = Eval.me(exp);
        final Service service = (Service) obj;

        System.out.println(service);

    }

    @Override
    public String toString() {
        return "Service [name=" + name + ", icon=" + icon + "]";
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public ServiceLifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(final ServiceLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }

    public void setUserInterface(final UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public String getImageTemplate() {
        return imageTemplate;
    }

    public void setImageTemplate(final String imageTemplate) {
        this.imageTemplate = imageTemplate;
    }

    public String getDefaultScalingUnit() {
        return defaultScalingUnit;
    }

    public void setDefaultScalingUnit(final String defaultScalingUnit) {
        this.defaultScalingUnit = defaultScalingUnit;
    }

    public boolean isSupportsScaling() {
        return supportsScaling;
    }

    public void setSupportsScaling(final boolean supportsScaling) {
        this.supportsScaling = supportsScaling;
    }

    public void setPlugins(List<PluginDescriptor> plugins) {
        this.plugins = plugins;
    }

    public List<PluginDescriptor> getPlugins() {
        return plugins;
    }

    public int getNumInstances() {
        return numInstances;
    }

    public void setNumInstances(int numInstances) {
        this.numInstances = numInstances;
    }

    public long getMaxJarSize() {
        return maxJarSize;
    }

    public void setMaxJarSize(long maxJarSize) {
        this.maxJarSize = maxJarSize;
    }

    public boolean isKeepFile() {
        return keepFile;
    }

    public void setKeepFile(boolean keepFile) {
        this.keepFile = keepFile;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public String getPidFile() {
        return pidFile;
    }

    public void setPidFile(String pidFile) {
        this.pidFile = pidFile;
    }

}
