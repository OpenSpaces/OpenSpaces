package org.openspaces.itest.pu.container.integrated.simple;

import org.openspaces.core.config.BeanLevelProperties;
import org.openspaces.core.config.BeanLevelMergedPropertiesAware;
import org.openspaces.core.config.BeanLevelPropertiesAware;

import java.util.Properties;

/**
 * @author kimchy
 */
public class TestBean2 implements BeanLevelMergedPropertiesAware, BeanLevelPropertiesAware {

    private String value;

    private BeanLevelProperties beanLevelProperties;

    private Properties mergedProperties;

    public BeanLevelProperties getBeanLevelProperties() {
        return beanLevelProperties;
    }

    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.mergedProperties = beanLevelProperties;
    }

    public Properties getMergedProperties() {
        return mergedProperties;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
