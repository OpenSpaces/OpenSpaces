package org.openspaces.admin.internal.pu.elastic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.bean.EnabledBeanConfigCannotBeChangedException;
import org.openspaces.core.util.StringProperties;


public class FlattenedBeanConfigPropertiesManager implements BeanConfigPropertiesManager {

    private final StringProperties properties;
    private final String enabledClassnameKey;
    private final String classnamesKey;

    public FlattenedBeanConfigPropertiesManager(String classnamesKey, String enabledClassnameKey, Map<String,String> properties) {
        this.properties = new StringProperties(properties);
        this.enabledClassnameKey = enabledClassnameKey;
        this.classnamesKey = classnamesKey;
    }

    public void setBeanConfig(String beanClassName, Map<String, String> properties) throws BeanConfigNotFoundException {
        
        if (isBeanEnabled(beanClassName)) {
            throw new EnabledBeanConfigCannotBeChangedException("Cannot modify bean [" + beanClassName + "] configuration while it is enabled. Disable it first.");
        }
        
        this.properties.putMap(beanClassName + ".", properties);
        addBeanInternal(beanClassName);
    }

   public void enableBean(String beanClassName) throws BeanConfigNotFoundException {
        if (!containsBeanInternal(beanClassName)) {
            throw new BeanConfigNotFoundException("Failed to enable bean [" + beanClassName + "] since it does not exist.");
        }
        properties.putArray(enabledClassnameKey,
                new String[] { beanClassName }, ",");
    }

    public void disableBean(String beanClassName) throws BeanConfigNotFoundException {

        if (!containsBeanInternal(beanClassName)) {
            throw new BeanConfigNotFoundException("Failed to disable bean [" + beanClassName + "] since it does not exist.");
        }
        
        List<String> enabled = Arrays.asList(getEnabledBeansClassNames());
        
        if (enabled.contains(beanClassName)) {
            enabled.remove(beanClassName);
            properties.putArray(enabledClassnameKey, enabled.toArray(new String[]{}) , ",");
        }
    }

    public boolean removeBeanConfig(String beanClassName) {
        
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
        return properties.getArray(enabledClassnameKey, ",", new String[] {});
    }

    public Map<String, String> getBeanConfig(String beanClassName) throws BeanConfigNotFoundException {
        
        if (!containsBeanInternal(beanClassName)) {
            throw new BeanConfigNotFoundException("Failed to get bean [" + beanClassName + "] since it does not exist.");
        }
        return properties.getMap(beanClassName, new HashMap<String, String>());
    }

    public String[] getBeansClassNames() {
        return properties.getArray(classnamesKey, ",", new String[] {});
    }
    
    public void disableAllBeans() {
        properties.remove(enabledClassnameKey);
    }

    private void addBeanInternal(String beanClassName) {
        List<String> beanList = Arrays.asList(getBeansClassNames());
        if (!beanList.contains(beanClassName)) {
            beanList.add(beanClassName);
            this.properties.putArray(
                    enabledClassnameKey, beanList.toArray(new String[] {}), ",");
        }
    }

    private boolean removeBeanInternal(String beanClassName) {
        boolean removed = false;
        List<String> beanList = Arrays.asList(getBeansClassNames());
        if (beanList.contains(beanClassName)) {
            beanList.remove(beanClassName);
            this.properties.putArray(
                    enabledClassnameKey, beanList.toArray(new String[] {}), ",");
            removed = true;
        }
        return removed;
    }

    private boolean containsBeanInternal(String beanClassName) {
        return Arrays.asList(getEnabledBeansClassNames()).contains(beanClassName);
    }

}
