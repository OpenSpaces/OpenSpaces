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

import com.j_spaces.jms.GSTopicImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A Spring factory bean to create {@link com.j_spaces.jms.GSTopicImpl} based on
 * a topic name.
 *
 * @author kimchy
 */
public class GigaSpaceTopic implements FactoryBean, InitializingBean {

    private String topicName;


    private GSTopicImpl topic;

    /**
     * The topic name for this JMS topic.
     */
    public void setName(String topicName) {
        this.topicName = topicName;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(topicName, "topicName proeprty is required");
        topic = new GSTopicImpl(topicName);
    }

    public Object getObject() throws Exception {
        return topic;
    }

    public Class getObjectType() {
        return topic == null ? GSTopicImpl.class : topic.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}