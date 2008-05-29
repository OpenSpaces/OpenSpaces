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
import org.openspaces.events.TransactionalEventContainer;
import org.openspaces.events.notify.NotifyBatch;
import org.openspaces.events.notify.NotifyContainer;
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
import org.springframework.core.annotation.AnnotationUtils;

/**
 * A {@link org.openspaces.events.notify.NotifyContainer} annotation post processor. Creates an intenral
 * instance of {@link org.openspaces.events.notify.SimpleNotifyEventListenerContainer} that wraps the given
 * bean (if annotated) listener.
 *
 * @author kimchy
 */
public class NotifyContainerAnnotationPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        Class<?> beanClass = this.getBeanClass(bean);

        NotifyContainer notifyContainer = AnnotationUtils.findAnnotation(beanClass, NotifyContainer.class);
        if (notifyContainer == null) {
            return bean;
        }

        EventContainersBus eventContainersBus = AnnotationProcessorUtils.findBus(applicationContext);

        GigaSpace gigaSpace = AnnotationProcessorUtils.findGigaSpace(bean, notifyContainer.gigaSpace(), applicationContext, beanName);

        SimpleNotifyContainerConfigurer notifyContainerConfigurer = new SimpleNotifyContainerConfigurer(gigaSpace);
        notifyContainerConfigurer.eventListenerAnnotation(bean);
        notifyContainerConfigurer.performSnapshot(notifyContainer.performSnapshot());

        notifyContainerConfigurer.ignoreEventOnNullTake(notifyContainer.ignoreEventOnNullTake());
        notifyContainerConfigurer.performTakeOnNotify(notifyContainer.performTakeOnNotify());

        notifyContainerConfigurer.comType(notifyContainer.commType().value());

        notifyContainerConfigurer.fifo(notifyContainer.fifo());

        if (!INotifyDelegatorFilter.class.equals(notifyContainer.notifyFilter())) {
            try {
                INotifyDelegatorFilter filter = notifyContainer.notifyFilter().newInstance();
                notifyContainerConfigurer.notifyFilter(filter);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create [" + notifyContainer.notifyFilter() + "]", e);
            }
        }

        // handle transactions
        TransactionalEventContainer transactional = AnnotationUtils.findAnnotation(beanClass, TransactionalEventContainer.class);
        if (transactional != null) {
            notifyContainerConfigurer.transactionManager(AnnotationProcessorUtils.findTxManager(transactional.transactionManager(), applicationContext, beanName));
            notifyContainerConfigurer.transactionIsolationLevel(transactional.isolation().value());
            notifyContainerConfigurer.transactionTimeout(transactional.timeout());
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