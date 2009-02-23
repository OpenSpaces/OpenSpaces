package org.openspaces.remoting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.util.AnnotationUtils;
import org.openspaces.remoting.scripting.AsyncScriptingExecutor;
import org.openspaces.remoting.scripting.ExecutorScriptingExecutor;
import org.openspaces.remoting.scripting.LazyLoadingRemoteInvocationAspect;
import org.openspaces.remoting.scripting.ScriptingExecutor;
import org.openspaces.remoting.scripting.ScriptingMetaArgumentsHandler;
import org.openspaces.remoting.scripting.ScriptingRemoteRoutingHandler;
import org.openspaces.remoting.scripting.SyncScriptingExecutor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kimchy
 */
public class RemotingAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware {

    private static final Log logger = LogFactory.getLog(RemotingAnnotationBeanPostProcessor.class);

    private ApplicationContext applicationContext;

    private Map<String, GigaSpace> gsByName;

    private GigaSpace uniqueGs;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, String beanName) throws BeansException {
        Class beanClass = AopUtils.getTargetClass(bean);
        if (beanClass == null) {
            return true;
        }
        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                SyncScriptingExecutor syncScriptingExecutor = field.getAnnotation(SyncScriptingExecutor.class);
                if (syncScriptingExecutor != null) {
                    SyncSpaceRemotingProxyFactoryBean factoryBean = new SyncSpaceRemotingProxyFactoryBean();
                    factoryBean.setGigaSpace(findGigaSpaceByName(syncScriptingExecutor.gigaSpace()));
                    factoryBean.setMetaArgumentsHandler(new ScriptingMetaArgumentsHandler());
                    factoryBean.setRemoteInvocationAspect(new LazyLoadingRemoteInvocationAspect());
                    factoryBean.setRemoteRoutingHandler(new ScriptingRemoteRoutingHandler());
                    factoryBean.setServiceInterface(ScriptingExecutor.class);
                    factoryBean.afterPropertiesSet();
                    field.setAccessible(true);
                    field.set(bean, factoryBean.getObject());
                }
                ExecutorScriptingExecutor executorScriptingExecutor = field.getAnnotation(ExecutorScriptingExecutor.class);
                if (executorScriptingExecutor != null) {
                    ExecutorSpaceRemotingProxyFactoryBean factoryBean = new ExecutorSpaceRemotingProxyFactoryBean();
                    factoryBean.setGigaSpace(findGigaSpaceByName(executorScriptingExecutor.gigaSpace()));
                    factoryBean.setTimeout(executorScriptingExecutor.timeout());
                    factoryBean.setMetaArgumentsHandler(new ScriptingMetaArgumentsHandler());
                    factoryBean.setRemoteInvocationAspect(new LazyLoadingRemoteInvocationAspect());
                    factoryBean.setRemoteRoutingHandler(new ScriptingRemoteRoutingHandler());
                    factoryBean.setServiceInterface(ScriptingExecutor.class);
                    factoryBean.afterPropertiesSet();
                    field.setAccessible(true);
                    field.set(bean, factoryBean.getObject());
                }
                AsyncScriptingExecutor asyncScriptingExecutor = field.getAnnotation(AsyncScriptingExecutor.class);
                if (asyncScriptingExecutor != null) {
                    AsyncSpaceRemotingProxyFactoryBean factoryBean = new AsyncSpaceRemotingProxyFactoryBean();
                    factoryBean.setTimeout(asyncScriptingExecutor.timeout());
                    factoryBean.setFifo(asyncScriptingExecutor.fifo());
                    factoryBean.setGigaSpace(findGigaSpaceByName(asyncScriptingExecutor.gigaSpace()));
                    factoryBean.setMetaArgumentsHandler(new ScriptingMetaArgumentsHandler());
                    factoryBean.setRemoteInvocationAspect(new LazyLoadingRemoteInvocationAspect());
                    factoryBean.setRemoteRoutingHandler(new ScriptingRemoteRoutingHandler());
                    factoryBean.setServiceInterface(ScriptingExecutor.class);
                    factoryBean.afterPropertiesSet();
                    field.setAccessible(true);
                    field.set(bean, factoryBean.getObject());
                }
                AsyncProxy asyncProxy = field.getAnnotation(AsyncProxy.class);
                if (asyncProxy != null) {
                    AsyncSpaceRemotingProxyFactoryBean factoryBean = new AsyncSpaceRemotingProxyFactoryBean();
                    factoryBean.setTimeout(asyncProxy.timeout());
                    factoryBean.setFifo(asyncProxy.fifo());
                    factoryBean.setGigaSpace(findGigaSpaceByName(asyncProxy.gigaSpace()));
                    factoryBean.setAsyncMethodPrefix(asyncProxy.asyncMethodPrefix());
                    factoryBean.setMetaArgumentsHandler((MetaArgumentsHandler) createByClassOrFindByName(asyncProxy.metaArgumentsHandler(), asyncProxy.metaArgumentsHandlerType()));
                    factoryBean.setRemoteInvocationAspect((RemoteInvocationAspect) createByClassOrFindByName(asyncProxy.remoteInvocationAspect(), asyncProxy.remoteInvocationAspectType()));
                    factoryBean.setRemoteRoutingHandler((RemoteRoutingHandler) createByClassOrFindByName(asyncProxy.remoteRoutingHandler(), asyncProxy.remoteRoutingHandlerType()));
                    factoryBean.setServiceInterface(field.getType());
                    factoryBean.afterPropertiesSet();
                    field.setAccessible(true);
                    field.set(bean, factoryBean.getObject());
                }
                SyncProxy syncProxy = field.getAnnotation(SyncProxy.class);
                if (syncProxy != null) {
                    SyncSpaceRemotingProxyFactoryBean factoryBean = new SyncSpaceRemotingProxyFactoryBean();
                    factoryBean.setGigaSpace(findGigaSpaceByName(syncProxy.gigaSpace()));
                    factoryBean.setBroadcast(syncProxy.broadcast());
                    factoryBean.setMetaArgumentsHandler((MetaArgumentsHandler) createByClassOrFindByName(syncProxy.metaArgumentsHandler(), syncProxy.metaArgumentsHandlerType()));
                    factoryBean.setRemoteInvocationAspect((RemoteInvocationAspect) createByClassOrFindByName(syncProxy.remoteInvocationAspect(), syncProxy.remoteInvocationAspectType()));
                    factoryBean.setRemoteRoutingHandler((RemoteRoutingHandler) createByClassOrFindByName(syncProxy.remoteRoutingHandler(), syncProxy.remoteRoutingHandlerType()));
                    factoryBean.setRemoteResultReducer((RemoteResultReducer) createByClassOrFindByName(syncProxy.remoteResultReducer(), syncProxy.remoteResultReducerType()));
                    factoryBean.setReturnFirstResult(syncProxy.returnFirstResult());
                    factoryBean.setServiceInterface(field.getType());
                    factoryBean.afterPropertiesSet();
                    field.setAccessible(true);
                    field.set(bean, factoryBean.getObject());
                }
                ExecutorProxy executorProxy = field.getAnnotation(ExecutorProxy.class);
                if (executorProxy != null) {
                    ExecutorSpaceRemotingProxyFactoryBean factoryBean = new ExecutorSpaceRemotingProxyFactoryBean();
                    factoryBean.setGigaSpace(findGigaSpaceByName(executorProxy.gigaSpace()));
                    factoryBean.setTimeout(executorProxy.timeout());
                    factoryBean.setBroadcast(executorProxy.broadcast());
                    factoryBean.setMetaArgumentsHandler((MetaArgumentsHandler) createByClassOrFindByName(executorProxy.metaArgumentsHandler(), executorProxy.metaArgumentsHandlerType()));
                    factoryBean.setRemoteInvocationAspect((RemoteInvocationAspect) createByClassOrFindByName(executorProxy.remoteInvocationAspect(), executorProxy.remoteInvocationAspectType()));
                    factoryBean.setRemoteRoutingHandler((RemoteRoutingHandler) createByClassOrFindByName(executorProxy.remoteRoutingHandler(), executorProxy.remoteRoutingHandlerType()));
                    factoryBean.setRemoteResultReducer((RemoteResultReducer) createByClassOrFindByName(executorProxy.remoteResultReducer(), executorProxy.remoteResultReducerType()));
                    factoryBean.setReturnFirstResult(executorProxy.returnFirstResult());
                    factoryBean.setServiceInterface(field.getType());
                    factoryBean.afterPropertiesSet();
                    field.setAccessible(true);
                    field.set(bean, factoryBean.getObject());
                }
            }
        });


        RemotingService remotingService = AnnotationUtils.findAnnotation(beanClass, RemotingService.class);
        if (remotingService != null) {
            SpaceRemotingServiceExporter exporter;
            if (StringUtils.hasLength(remotingService.exporter())) {
                exporter = (SpaceRemotingServiceExporter) applicationContext.getBean(remotingService.exporter());
                if (exporter == null) {
                    throw new IllegalArgumentException("Failed to find exporter under name [" + remotingService.exporter() + "] for bean [" + beanName + "]");
                }
            } else {
                Map exporters = applicationContext.getBeansOfType(SpaceRemotingServiceExporter.class);
                if (exporters.isEmpty()) {
                    throw new IllegalArgumentException("No service exporters are defined within the context, can't register remote service bean [" + beanName + "]");
                }
                if (exporters.size() > 1) {
                    throw new IllegalStateException("More than one service exporter are defined within the context, please specify the exact service exported to register with");
                }
                exporter = (SpaceRemotingServiceExporter) exporters.values().iterator().next();
            }
            exporter.addService(beanName, bean);
        }
        return true;
    }

    protected Object createByClassOrFindByName(String name, Class clazz) throws NoSuchBeanDefinitionException {
        if (StringUtils.hasLength(name)) {
            return applicationContext.getBean(name);
        }

        if (!Object.class.equals(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new NoSuchBeanDefinitionException("Failed to create class [" + clazz + "]");
            }
        }
        return null;
    }

    protected GigaSpace findGigaSpaceByName(String gsName) throws NoSuchBeanDefinitionException {
        initMapsIfNecessary();
        if (gsName == null || "".equals(gsName)) {
            if (this.uniqueGs != null) {
                return this.uniqueGs;
            } else {
                throw new NoSuchBeanDefinitionException("No GigaSpaces name given and factory contains several");
            }
        }
        GigaSpace namedGs = this.gsByName.get(gsName);
        if (namedGs == null) {
            throw new NoSuchBeanDefinitionException("No GigaSpaces found for name [" + gsName + "]");
        }
        return namedGs;
    }

    /**
     * Lazily initialize gs map.
     */
    private synchronized void initMapsIfNecessary() {
        if (this.gsByName == null) {
            this.gsByName = new HashMap<String, GigaSpace>();
            // Look for named GigaSpaces

            for (String gsName : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.applicationContext, GigaSpace.class)) {

                GigaSpace gs = (GigaSpace) this.applicationContext.getBean(gsName);
                gsByName.put(gsName, gs);
            }

            if (this.gsByName.isEmpty()) {
                // Try to find a unique GigaSpaces.
                String[] gsNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.applicationContext, GigaSpace.class);
                if (gsNames.length == 1) {
                    this.uniqueGs = (GigaSpace) this.applicationContext.getBean(gsNames[0]);
                }
            } else if (this.gsByName.size() == 1) {
                this.uniqueGs = this.gsByName.values().iterator().next();
            }

            if (this.gsByName.isEmpty() && this.uniqueGs == null) {
                logger.warn("No named gs instances defined and not exactly one anonymous one: cannot inject");
            }
        }
    }
}
