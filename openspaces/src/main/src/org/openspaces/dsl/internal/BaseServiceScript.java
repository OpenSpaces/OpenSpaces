package org.openspaces.dsl.internal;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.openspaces.dsl.CustomCommand;
import org.openspaces.dsl.PluginDescriptor;
import org.openspaces.dsl.Service;
import org.openspaces.dsl.ServiceLifecycle;
import org.openspaces.dsl.ui.BalanceGauge;
import org.openspaces.dsl.ui.BarLineChart;
import org.openspaces.dsl.ui.MetricGroup;
import org.openspaces.dsl.ui.UserInterface;
import org.openspaces.dsl.ui.WidgetGroup;

public abstract class BaseServiceScript extends Script {

    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(BaseServiceScript.class.getName());
    // protected ServiceContext context;

    private Object activeObject = null;

    private Map<String, Method> activeMethods;
    private Service service;

    @Override
    public void setProperty(final String name, final Object value) {
        methodMissing(name, new Object[] { value });
    }

    @SuppressWarnings("unchecked")
    public Object methodMissing(final String name, final Object args) {
        final Object[] argsArray = (Object[]) args;

        // first check if this is an object declaration
        final Object obj = createDslObject(name);
        if (obj != null) {

            if (this.activeObject != null) {
                final Collection<Method> methods = this.activeMethods.values();
                for (final Method method : methods) {
                    if (method.getName().startsWith("set") && (method.getParameterTypes().length == 1)
                                && (method.getParameterTypes()[0].equals(obj.getClass()))) {

                        try {
                            method.invoke(this.activeObject, new Object[] { obj });
                        } catch (final Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            swapActiveObject((Closure<Object>) argsArray[0], obj);
            return obj;
        }

        // next check if this is a property assignment

        if (argsArray.length != 1) {
            throw new MissingMethodException(name, Service.class, argsArray);
        }

        final Object arg = argsArray[0];

        final String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);

        try {
            final Method m = this.activeMethods.get(methodName);
            if (m != null) {
                m.invoke(this.activeObject, arg);
            } else {
                logger.severe("Method " + methodName + " not found on object: " + this.activeObject);
                throw new MissingMethodException(name, this.activeObject.getClass(), new Object[0]);

            }
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to invoke method " + methodName, e);
            throw new IllegalStateException("Failed to invoke method " + methodName
                    + " on object " + this.activeObject, e);
        }

        return this.activeObject;
    }

    public Service service(final Closure<Object> closure) {
        this.service = new Service();
        // if (context == null) {
        // context = new ServiceContext(service, null, null); //TODO - fix this
        // }

        swapActiveObject(closure, service);
        return service;

    }

    public ServiceLifecycle lifecycle(final Closure<Object> closure) {
        final ServiceLifecycle sl = new ServiceLifecycle();

        swapActiveObject(closure, sl);

        this.service.setLifecycle(sl);
        // this.context.getService().setLifecycle(sl);
        return sl;

    }

    public Object createDslObject(final String name) {
        if (name.equals("userInterface")) {
            return new UserInterface();
        } else if (name.equals("plugin")) {
            return new PluginDescriptor();
        } else if (name.equals("metricGroup")) {
            return new MetricGroup();
        } else if (name.equals("widgetGroup")) {
            return new WidgetGroup();
        } else if (name.equals("balanceGauge")) {
            return new BalanceGauge();
        } else if (name.equals("barLineChart")) {
            return new BarLineChart();
        } else if (name.equals("customCommand")) {
            return new CustomCommand();
        }

        return null;
    }

    protected Object swapActiveObject(final Closure<Object> closure, final Object obj) {
        final Object prevObject = this.activeObject;
        this.activeObject = obj;
        final Map<String, Method> prevMethods = this.activeMethods;
        this.activeMethods = new HashMap<String, Method>();
        final Method[] methods = this.activeObject.getClass().getMethods();
        for (final Method method : methods) {
            this.activeMethods.put(method.getName(), method);
        }

        closure.setResolveStrategy(Closure.OWNER_ONLY);
        final Object res = closure.call();
        activeObject = prevObject;
        this.activeMethods = prevMethods;
        return res;
    }
    
    public void println(Object obj) {        
        logger.info(obj.toString());
    }

}
