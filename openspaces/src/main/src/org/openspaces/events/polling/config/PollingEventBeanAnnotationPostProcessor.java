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
import org.openspaces.events.TransactionalEventBean;
import org.openspaces.events.polling.PollingEventBean;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.support.AnnotationProcessorUtils;
import org.openspaces.events.support.EventContainersBus;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A {@link org.openspaces.events.polling.PollingEventBean} annotation post processor. Creates an intenral
 * instance of {@link org.openspaces.events.polling.SimplePollingEventListenerContainer} that wraps the given
 * bean (if annotated) listener.
 *
 * @author kimchy
 */
public class PollingEventBeanAnnotationPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        Class<?> beanClass = this.getBeanClass(bean);

        PollingEventBean pollingEventBean = AnnotationUtils.findAnnotation(beanClass, PollingEventBean.class);
        if (pollingEventBean == null) {
            return bean;
        }

        EventContainersBus eventContainersBus = AnnotationProcessorUtils.findBus(applicationContext);

        GigaSpace gigaSpace = AnnotationProcessorUtils.findGigaSpace(bean, pollingEventBean.gigaSpace(), applicationContext, beanName);

        SimplePollingContainerConfigurer pollingContainerConfigurer = new SimplePollingContainerConfigurer(gigaSpace);
        if (bean instanceof SpaceDataEventListener) {
            pollingContainerConfigurer.eventListener((SpaceDataEventListener) bean);
        } else {
            pollingContainerConfigurer.eventListenerAnnotation(bean);
        }
        pollingContainerConfigurer.concurrentConsumers(pollingEventBean.concurrentConsumers());
        pollingContainerConfigurer.maxConcurrentConsumers(pollingEventBean.maxConcurrentConsumers());
        pollingContainerConfigurer.receiveTimeout(pollingEventBean.receiveTimeout());
        pollingContainerConfigurer.performSnapshot(pollingEventBean.performSnapshot());
        pollingContainerConfigurer.passArrayAsIs(pollingEventBean.passArrayAsIs());
        pollingContainerConfigurer.recoveryInterval(pollingEventBean.recoveryInterval());

        // handle transactions (we support using either @Transactional or @TransactionalEventBean or both)
        TransactionalEventBean transactionalEventBean = AnnotationUtils.findAnnotation(beanClass, TransactionalEventBean.class);
        Transactional transactional = AnnotationUtils.findAnnotation(beanClass, Transactional.class);
        if (transactionalEventBean != null || transactional != null) {
            if (transactionalEventBean != null) {
                pollingContainerConfigurer.transactionManager(AnnotationProcessorUtils.findTxManager(transactionalEventBean.transactionManager(), applicationContext, beanName));
            } else {
                pollingContainerConfigurer.transactionManager(AnnotationProcessorUtils.findTxManager("", applicationContext, beanName));
            }
            Isolation isolation = Isolation.DEFAULT;
            if (transactional != null && transactional.isolation() != Isolation.DEFAULT) {
                isolation = transactional.isolation();
            }
            if (transactionalEventBean != null && transactionalEventBean.isolation() != Isolation.DEFAULT) {
                isolation = transactionalEventBean.isolation();
            }
            pollingContainerConfigurer.transactionIsolationLevel(isolation.value());

            int timeout = TransactionDefinition.TIMEOUT_DEFAULT;
            if (transactional != null && transactional.timeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
                timeout = transactional.timeout();
            }
            if (transactionalEventBean != null && transactionalEventBean.timeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
                timeout = transactionalEventBean.timeout();
            }
            pollingContainerConfigurer.transactionTimeout(timeout);
        }

        eventContainersBus.registerContaienr(beanName, pollingContainerConfigurer.pollingContainer());

        return bean;
    }

    private Class<?> getBeanClass(Object bean) {
        return AopUtils.getTargetClass(bean);
    }
}
