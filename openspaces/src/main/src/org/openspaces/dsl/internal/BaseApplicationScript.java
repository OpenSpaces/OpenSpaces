package org.openspaces.dsl.internal;

import groovy.lang.Closure;
import groovy.lang.Script;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.openspaces.dsl.Application;

public abstract class BaseApplicationScript extends Script {

    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(BaseApplicationScript.class.getName());

    private Object activeObject = null;
    private Application application;

    @Override
    public void setProperty(final String name, final Object value) {
        try {
            BeanUtils.getProperty(this.activeObject, name); // first check that property exists
            BeanUtils.setProperty(this.activeObject, name, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to set property " + name + " to " + value, e);
        } catch (final InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to set property " + name + " to " + value, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Property " + name + " does not exist in class: " + this.activeObject.getClass().getName(), e);
        }
    }

    public Object methodMissing(final String name, final Object args) {
        return null;
        // final Object[] argsArray = (Object[]) args;
        //
        // // first check if this is an object declaration
        // final Object obj = createDslObject(name);
        // if (obj != null) {
        //
        // if (this.activeObject != null) {
        // final Collection<Method> methods = this.activeMethods.values();
        // for (final Method method : methods) {
        // if (method.getName().startsWith("set") && (method.getParameterTypes().length == 1)
        // && (method.getParameterTypes()[0].equals(obj.getClass()))) {
        //
        // try {
        // method.invoke(this.activeObject, new Object[] { obj });
        // } catch (final Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // break;
        // }
        // }
        // }
        // swapActiveObject((Closure<Object>) argsArray[0], obj);
        // return obj;
        // }
        //
        // // next check if this is a property assignment
        //
        // if (argsArray.length != 1) {
        // throw new MissingMethodException(name, Service.class, argsArray);
        // }
        //
        // final Object arg = argsArray[0];
        //
        // final String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        //
        // try {
        // final Method m = this.activeMethods.get(methodName);
        // if (m != null) {
        // m.invoke(this.activeObject, arg);
        // } else {
        // logger.severe("Method " + methodName + " not found on object: " + this.activeObject);
        // throw new MissingMethodException(name, this.activeObject.getClass(), new Object[0]);
        //
        // }
        // } catch (final Exception e) {
        // logger.log(Level.SEVERE, "Failed to invoke method " + methodName, e);
        // throw new IllegalStateException("Failed to invoke method " + methodName
        // + " on object " + this.activeObject, e);
        // }
        //
        // return this.activeObject;
    }

    public Application application(final Closure<Object> closure) {
        this.application = new Application();
        // if (context == null) {
        // context = new ServiceContext(service, null, null); //TODO - fix this
        // }

        this.activeObject = this.application;
        closure.call();
        return this.application;

    }

    @Override
    public void println(final Object obj) {
        logger.info(obj.toString());
    }

}
