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

package org.openspaces.pu.sla.monitor;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * The bean property monitor allows to register a Spring bean reference and a proeprty name which will
 * be monitored at a scheduled interval by invoking it.
 *
 * @author kimchy
 */
public class BeanPropertyMonitor extends AbstractMonitor implements ApplicationContextMonitor {

    private static final long serialVersionUID = -6401809881237930697L;

    private String ref;

    private String propertyName;


    private transient MethodInvoker methodInvoker;

    /**
     * Sets the registered bean reference that will be monitored.
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * The registered bean reference that will be monitored.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the property name of the given bean that will be monitored.
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * The property name of the given bean that will be monitored.
     */
    public String getPropertyName() {
        return propertyName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        Assert.notNull(getName(), "name property is required");
        Assert.notNull(ref, "ref property is required");
        Assert.notNull(propertyName, "propertyName property is required");
        Object bean;
        try {
            bean = applicationContext.getBean(ref);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException("Monitor did not find bean [" + ref + "] under Spring application context, availalbe beans are " + Arrays.toString(applicationContext.getBeanDefinitionNames()));
        }

        methodInvoker = new MethodInvoker();
        methodInvoker.setTargetMethod("get" + StringUtils.capitalize(propertyName));
        methodInvoker.setTargetObject(bean);
        try {
            methodInvoker.prepare();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to find class for bean [" + bean + "]", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find method for bean [" + bean + "]", e);
        }
    }

    public double getValue() {
        try {
            Number number = (Number) methodInvoker.invoke();
            return number.doubleValue();
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to invoke method", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to invoke method", e);
        }
    }
}
