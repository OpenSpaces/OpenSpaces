package org.openspaces.events.support;

import org.openspaces.core.GigaSpace;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kimchy
 */
public class AnnotationProcessorUtils {

    private AnnotationProcessorUtils() {

    }

    public static EventContainersBus findBus(ApplicationContext applicationContext) {
        Map eventContainerBuses = applicationContext.getBeansOfType(EventContainersBus.class);
        if (eventContainerBuses.isEmpty()) {
            throw new IllegalArgumentException("No event container bus found, can't register polling container");
        } else if (eventContainerBuses.size() > 1) {
            throw new IllegalArgumentException("Multiple event container buses found, can't register polling container");
        }
        return (EventContainersBus) eventContainerBuses.values().iterator().next();
    }

    public static GigaSpace findGigaSpace(final Object bean, String gigaSpaceName, ApplicationContext applicationContext, String beanName) throws IllegalArgumentException {
        GigaSpace gigaSpace = null;
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        if (StringUtils.hasLength(gigaSpaceName)) {
            gigaSpace = (GigaSpace) applicationContext.getBean(gigaSpaceName);
            if (gigaSpace == null) {
                throw new IllegalArgumentException("Failed to lookup GigaSpace using name [" + gigaSpaceName + "] in bean [" + beanName + "]");
            }
            return gigaSpace;
        }

        // try and automatically find a GigaSpace instance
        final Map<String, GigaSpace> fieldsGigaSpace = new HashMap<String, GigaSpace>();
        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (GigaSpace.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    fieldsGigaSpace.put(field.getName(), (GigaSpace) field.get(bean));
                }
            }
        });

        if (!fieldsGigaSpace.isEmpty()) {
            // we have a field that has GigaSpace, use that one
            if (fieldsGigaSpace.size() == 1) {
                gigaSpace = fieldsGigaSpace.values().iterator().next();
            } else {
                // prefer one that is called "space" or "gigaSpace"
                gigaSpace = fieldsGigaSpace.get("gigaSpace");
                if (gigaSpace == null) {
                    gigaSpace = fieldsGigaSpace.get("space");
                }
                if (gigaSpace == null) {
                    throw new IllegalArgumentException("Failed to resolve GigaSpace to use with polling contaienr, " +
                            "[" + beanName + "] has several GigaSpace fields to choose from, and none is called 'space' or 'gigaSpace'");
                }
            }
        } else {
            Map gigaSpaces = applicationContext.getBeansOfType(GigaSpace.class);
            if (gigaSpaces.size() == 1) {
                gigaSpace = (GigaSpace) gigaSpaces.values().iterator().next();
            } else {
                throw new IllegalArgumentException("Failed to resolve GigaSpace to use with polling container, " +
                        "[" + beanName + "] does not specifiy one, has no fields of that type, and there are more than one GigaSpace beans within the context");
            }
        }
        return gigaSpace;
    }

    public static PlatformTransactionManager findTxManager(String txManagerName, ApplicationContext applicationContext, String beanName) {
        PlatformTransactionManager txManager;
        if (StringUtils.hasLength(txManagerName)) {
            txManager = (PlatformTransactionManager) applicationContext.getBean(txManagerName);
            if (txManager == null) {
                throw new IllegalArgumentException("Failed to find transaction manager for name [" + txManagerName + "] in bean [" + beanName + "]");
            }
        } else {
            Map txManagers = applicationContext.getBeansOfType(PlatformTransactionManager.class);
            if (txManagers.size() == 1) {
                txManager = (PlatformTransactionManager) txManagers.values().iterator().next();
            } else {
                throw new IllegalArgumentException("More than one transaction manager defined in context, and no transactionManager specified, can't detect one");
            }
        }
        return txManager;
    }
}
