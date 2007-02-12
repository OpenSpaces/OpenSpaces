package org.openspaces.pu.container.support;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author kimchy
 */
public class ResourceApplicationContext extends AbstractXmlApplicationContext {

    private Resource[] resources;

    private List beanPostProcessors = new ArrayList();

    public ResourceApplicationContext(Resource[] resources, ApplicationContext parent) {
        super(parent);
        this.resources = resources;
    }

    protected Resource[] getConfigResources() {
        return this.resources;
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.add(beanPostProcessor);
    }

    protected DefaultListableBeanFactory createBeanFactory() {
        DefaultListableBeanFactory beanFactory = super.createBeanFactory();
        for (Iterator it = beanPostProcessors.iterator(); it.hasNext();) {
            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
        return beanFactory;
    }
}
