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

package org.openspaces.events.polling.config;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.util.AnnotationUtils;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.TransactionalEventContainer;
import org.openspaces.events.polling.PollingContainer;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.support.AnnotationProcessorUtils;
import org.openspaces.events.support.EventContainersBus;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A {@link org.openspaces.events.polling.PollingContainer} annotation post processor. Creates an intenral
 * instance of {@link org.openspaces.events.polling.SimplePollingEventListenerContainer} that wraps the given
 * bean (if annotated) listener.
 *
 * @author kimchy
 */
public class PollingContainerAnnotationPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        Class<?> beanClass = this.getBeanClass(bean);

        PollingContainer pollingContainer = AnnotationUtils.findAnnotation(beanClass, PollingContainer.class);
        if (pollingContainer == null) {
            return bean;
        }

        EventContainersBus eventContainersBus = AnnotationProcessorUtils.findBus(applicationContext);

        GigaSpace gigaSpace = AnnotationProcessorUtils.findGigaSpace(bean, pollingContainer.gigaSpace(), applicationContext, beanName);

        SimplePollingContainerConfigurer pollingContainerConfigurer = new SimplePollingContainerConfigurer(gigaSpace);
        if (bean instanceof SpaceDataEventListener) {
            pollingContainerConfigurer.eventListener((SpaceDataEventListener) bean);
        } else {
            pollingContainerConfigurer.eventListenerAnnotation(bean);
        }
        pollingContainerConfigurer.concurrentConsumers(pollingContainer.concurrentConsumers());
        pollingContainerConfigurer.maxConcurrentConsumers(pollingContainer.maxConcurrentConsumers());
        pollingContainerConfigurer.receiveTimeout(pollingContainer.receiveTimeout());
        pollingContainerConfigurer.performSnapshot(pollingContainer.performSnapshot());
        pollingContainerConfigurer.passArrayAsIs(pollingContainer.passArrayAsIs());
        pollingContainerConfigurer.recoveryInterval(pollingContainer.recoveryInterval());

        // handle transactions
        TransactionalEventContainer transactional = AnnotationUtils.findAnnotation(beanClass, TransactionalEventContainer.class);
        if (transactional != null) {
            pollingContainerConfigurer.transactionManager(AnnotationProcessorUtils.findTxManager(transactional.transactionManager(), applicationContext, beanName));
            pollingContainerConfigurer.transactionIsolationLevel(transactional.isolation().value());
            pollingContainerConfigurer.transactionTimeout(transactional.timeout());
        }

        eventContainersBus.registerContaienr(beanName, pollingContainerConfigurer.pollingContainer());

        return bean;
    }

    private Class<?> getBeanClass(Object bean) {
        return AopUtils.getTargetClass(bean);
    }
}
