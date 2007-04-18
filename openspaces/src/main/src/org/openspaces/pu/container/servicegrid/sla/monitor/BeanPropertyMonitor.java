package org.openspaces.pu.container.servicegrid.sla.monitor;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * @author kimchy
 */
public class BeanPropertyMonitor extends AbstractMonitor implements ApplicationContextMonitor {

    private String ref;

    private String propertyName;


    private transient MethodInvoker methodInvoker;

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        Assert.notNull(getName(), "name property is required");
        Assert.notNull(ref, "ref property is required");
        Assert.notNull(propertyName, "propertyName property is required");
        Object bean = applicationContext.getBean(ref);
        if (bean == null) {
            throw new IllegalArgumentException("Monitor did not find bean [" + bean + "] under Spring application context");
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
