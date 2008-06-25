package org.openspaces.pu.container.web.context;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.web.WebApplicationContextProcessingUnitContainerProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author kimchy
 */
public class ProcessingUnitContextLoader extends ContextLoader {

    protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException {
        return (ApplicationContext) servletContext.getAttribute(WebApplicationContextProcessingUnitContainerProvider.APPLICATION_CONTEXT_CONTEXT);
    }

    protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent) throws BeansException {
        ProcessingUnitWebApplicationContext wac = new ProcessingUnitWebApplicationContext();

        BeanLevelProperties beanLevelProperties = (BeanLevelProperties) servletContext.getAttribute(WebApplicationContextProcessingUnitContainerProvider.BEAN_LEVEL_PROPERTIES_CONTEXT);
        if (beanLevelProperties != null) {
            wac.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties));
            wac.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }
        ClusterInfo clusterInfo = (ClusterInfo) servletContext.getAttribute(WebApplicationContextProcessingUnitContainerProvider.CLUSTER_INFO_CONTEXT);
        if (clusterInfo != null) {
            wac.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        wac.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));

        wac.setParent(parent);
        wac.setServletContext(servletContext);
        wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
        customizeContext(servletContext, wac);
        wac.refresh();
        return wac;

    }
}
