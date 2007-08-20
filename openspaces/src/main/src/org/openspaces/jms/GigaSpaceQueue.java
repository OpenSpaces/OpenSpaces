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

package org.openspaces.jms;

import com.j_spaces.jms.GSQueueImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A Spring factory bean to create {@link com.j_spaces.jms.GSQueueImpl} based on
 * a queue name.
 *
 * @author kimchy
 */
public class GigaSpaceQueue implements FactoryBean, InitializingBean {

    private String queueName;


    private GSQueueImpl queue;

    /**
     * The queue name for this JMS queue.
     */
    public void setName(String queueName) {
        this.queueName = queueName;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(queueName, "queueName proeprty is required");
        queue = new GSQueueImpl(queueName);
    }

    public Object getObject() throws Exception {
        return queue;
    }

    public Class getObjectType() {
        return queue == null ? GSQueueImpl.class : queue.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
