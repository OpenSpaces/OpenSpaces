/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.pu.container.integrated.simple;

import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;

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
