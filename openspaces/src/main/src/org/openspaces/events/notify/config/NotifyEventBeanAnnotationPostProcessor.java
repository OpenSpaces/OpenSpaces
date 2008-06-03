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

package org.openspaces.events.notify.config;

import com.j_spaces.core.client.INotifyDelegatorFilter;
import net.jini.lease.LeaseListener;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.util.AnnotationUtils;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.TransactionalEventBean;
import org.openspaces.events.notify.NotifyBatch;
import org.openspaces.events.notify.NotifyEventBean;
import org.openspaces.events.notify.NotifyLease;
import org.openspaces.events.notify.NotifyType;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
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
 * A {@link org.openspaces.events.notify.NotifyEventBean} annotation post processor. Creates an intenral
 * instance of {@link org.openspaces.events.notify.SimpleNotifyEventListenerContainer} that wraps the given
 * bean (if annotated) listener.
 *
 * @author kimchy
 */
public class NotifyEventBeanAnnotationPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        Class<?> beanClass = this.getBeanClass(bean);

        NotifyEventBean notifyEventBean = AnnotationUtils.findAnnotation(beanClass, NotifyEventBean.class);
        if (notifyEventBean == null) {
            return bean;
        }

        EventContainersBus eventContainersBus = AnnotationProcessorUtils.findBus(applicationContext);

        GigaSpace gigaSpace = AnnotationProcessorUtils.findGigaSpace(bean, notifyEventBean.gigaSpace(), applicationContext, beanName);

        SimpleNotifyContainerConfigurer notifyContainerConfigurer = new SimpleNotifyContainerConfigurer(gigaSpace);
        if (bean instanceof SpaceDataEventListener) {
            notifyContainerConfigurer.eventListener((SpaceDataEventListener) bean);
        } else {
            notifyContainerConfigurer.eventListenerAnnotation(bean);
        }
        notifyContainerConfigurer.performSnapshot(notifyEventBean.performSnapshot());

        notifyContainerConfigurer.ignoreEventOnNullTake(notifyEventBean.ignoreEventOnNullTake());
        notifyContainerConfigurer.performTakeOnNotify(notifyEventBean.performTakeOnNotify());

        notifyContainerConfigurer.comType(notifyEventBean.commType().value());

        notifyContainerConfigurer.fifo(notifyEventBean.fifo());

        if (!INotifyDelegatorFilter.class.equals(notifyEventBean.notifyFilter())) {
            try {
                INotifyDelegatorFilter filter = notifyEventBean.notifyFilter().newInstance();
                notifyContainerConfigurer.notifyFilter(filter);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create [" + notifyEventBean.notifyFilter() + "]", e);
            }
        }

        // handle transactions (we support using either @Transactional or @TransactionalEventBean or both)
        TransactionalEventBean transactionalEventBean = AnnotationUtils.findAnnotation(beanClass, TransactionalEventBean.class);
        Transactional transactional = AnnotationUtils.findAnnotation(beanClass, Transactional.class);
        if (transactionalEventBean != null || transactional != null) {
            if (transactionalEventBean != null) {
                notifyContainerConfigurer.transactionManager(AnnotationProcessorUtils.findTxManager(transactionalEventBean.transactionManager(), applicationContext, beanName));
            } else {
                notifyContainerConfigurer.transactionManager(AnnotationProcessorUtils.findTxManager("", applicationContext, beanName));
            }
            Isolation isolation = Isolation.DEFAULT;
            if (transactional != null && transactional.isolation() != Isolation.DEFAULT) {
                isolation = transactional.isolation();
            }
            if (transactionalEventBean != null && transactionalEventBean.isolation() != Isolation.DEFAULT) {
                isolation = transactionalEventBean.isolation();
            }
            notifyContainerConfigurer.transactionIsolationLevel(isolation.value());

            int timeout = TransactionDefinition.TIMEOUT_DEFAULT;
            if (transactional != null && transactional.timeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
                timeout = transactional.timeout();
            }
            if (transactionalEventBean != null && transactionalEventBean.timeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
                timeout = transactionalEventBean.timeout();
            }
            notifyContainerConfigurer.transactionTimeout(timeout);
        }

        NotifyType notifyType = AnnotationUtils.findAnnotation(beanClass, NotifyType.class);
        if (notifyType != null) {
            notifyContainerConfigurer.notifyWrite(notifyType.write());
            notifyContainerConfigurer.notifyUpdate(notifyType.update());
            notifyContainerConfigurer.notifyTake(notifyType.take());
            notifyContainerConfigurer.notifyLeaseExpire(notifyType.leaseExpire());
        }

        NotifyBatch notifyBatch = AnnotationUtils.findAnnotation(beanClass, NotifyBatch.class);
        if (notifyBatch != null) {
            notifyContainerConfigurer.batchSize(notifyBatch.size());
            notifyContainerConfigurer.batchTime(notifyBatch.time());
        }

        NotifyLease notifyLease = AnnotationUtils.findAnnotation(beanClass, NotifyLease.class);
        if (notifyLease != null) {
            notifyContainerConfigurer.autoRenew(true);
            notifyContainerConfigurer.listenerLease(notifyLease.lease());
            if (!LeaseListener.class.equals(notifyLease.leaseListener())) {
                try {
                    LeaseListener leaseListener = notifyLease.leaseListener().newInstance();
                    notifyContainerConfigurer.leaseListener(leaseListener);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to create [" + notifyLease.leaseListener() + "]", e);
                }
            }
        }

        eventContainersBus.registerContaienr(beanName, notifyContainerConfigurer.notifyContainer());

        return bean;
    }

    private Class<?> getBeanClass(Object bean) {
        return AopUtils.getTargetClass(bean);
    }
}