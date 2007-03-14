package org.openspaces.pu.container.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * A Spring {@link org.springframework.context.ApplicationContext} implementation that works with
 * Spring {@link org.springframework.core.io.Resource} for config locations.
 * 
 * <p>
 * By default this applicaiton does not "start" and requires explicit call to {@link #refresh()}.
 * 
 * @author kimchy
 */
public class ResourceApplicationContext extends AbstractXmlApplicationContext {

    private Resource[] resources;

    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

    /**
     * Create this application context with a list of resources for configuration and an optional
     * parent application context (can be <code>null</code>).
     * 
     * @param resources
     *            List of xml config resources
     * @param parent
     *            An optional parent application context
     */
    public ResourceApplicationContext(Resource[] resources, ApplicationContext parent) {
        super(parent);
        this.resources = resources;
    }

    /**
     * Returns the config resources this applicaiton context uses.
     */
    protected Resource[] getConfigResources() {
        return this.resources;
    }

    /**
     * Adds Spring bean post processor. Note, this method should be called before the
     * {@link #refresh()} is called on this application context for the bean post processor to take
     * affect.
     * 
     * @param beanPostProcessor
     *            The bean post processor to add
     */
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.add(beanPostProcessor);
    }

    /**
     * Creates a new bean factory by delegating to the super bean factory creation and then adding
     * all the registered {@link BeanPostProcessor}s.
     */
    protected DefaultListableBeanFactory createBeanFactory() {
        DefaultListableBeanFactory beanFactory = super.createBeanFactory();
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
        return beanFactory;
    }
}
