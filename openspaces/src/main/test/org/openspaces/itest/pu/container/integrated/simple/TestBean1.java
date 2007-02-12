package org.openspaces.itest.pu.container.integrated.simple;

import org.openspaces.core.config.BeanLevelMergedPropertiesAware;

import java.util.Properties;

/**
 * @author kimchy
 */
public class TestBean1 implements BeanLevelMergedPropertiesAware {

    private String value;

    private Properties beanLevelProperties;

    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public Properties getBeanLevelProperties() {
        return beanLevelProperties;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
