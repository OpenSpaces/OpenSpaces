package org.openspaces.pu.container.web.context;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class ProcessingUnitWebApplicationContext extends XmlWebApplicationContext {

    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

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
