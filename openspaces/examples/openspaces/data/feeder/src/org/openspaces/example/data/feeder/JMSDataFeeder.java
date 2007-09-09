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

package org.openspaces.example.data.feeder;

import org.openspaces.example.data.common.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A JMS feeder bean starts a scheduled task that uses Spring's JmsTemplate to write Data objects into the space.
 *
 * The JmsTemplate's JMS ConnectionFactory is injected with a MessageConverter that converts the JMS ObjectMessages
 * to the Data objects before writing them to the space. It is important to understand that the MessageConverter is not
 * the standard JmsTemplate's MessageConverter, but the GigaSpaces's implementation of MessageConverter.
 *
 * <p>The scheduled support uses the java.util.concurrent Scheduled Executor Service. It
 * is started and stopped based on Spring lifecycle events.
 *
 * @author shaiw
 */
public class JMSDataFeeder implements InitializingBean, DisposableBean {

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    private long numberOfTypes = 10;

    private long defaultDelay = 1000;

    private JMSDataFeederTask dataFeederTask;

    private Long instanceId;

    private long startIdFrom = 0;

    /**
     * The bean's JmsTemplate
     */
    private JmsTemplate jmsTemplate;


    /**
     * Sets the number of types that will be used to set {@link org.openspaces.example.data.common.Data#setType(Long)}.
     *
     * <p>The type is used as the routing index for partitioned space. This will affect the distribution of Data
     * objects over a partitioned space.
     */
    public void setNumberOfTypes(long numberOfTypes) {
        this.numberOfTypes = numberOfTypes;
    }

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Sets the JmsTemplate
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }


    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING FEEDER WITH CYCLE [" + defaultDelay + "]");
        if (instanceId != null) {
            // have a range of ids based on the instance id of the processing unit
            startIdFrom = instanceId * 100000000;
        }
        executorService = Executors.newScheduledThreadPool(1);
        dataFeederTask = new JMSDataFeederTask();
        sf = executorService.scheduleAtFixedRate(dataFeederTask, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
    }

    public void destroy() throws Exception {
        sf.cancel(true);
        sf = null;
        executorService.shutdown();
    }

    public class JMSDataFeederTask implements Runnable {

        private long counter = 1;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                final Data data = new Data((counter++ % numberOfTypes), "JMS_FEEDER " + Long.toString(time));
                data.setId(-(startIdFrom + counter));
                data.setProcessed(false);

                // Send to space using JmsTemplate. Because the ObjectMessage2ObjectConverter (MessageConverter)
                // is injected to the Connectionfactory, used by the JmsTemplate, what actually is written to the space
                // are the Data objects and not JMS ObjectMessages.
                jmsTemplate.send(new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(data);
                    }
                });
                System.out.println("--- JMS WROTE " + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public long getCounter() {
            return counter;
        }
    }

    public long getFeedCount() {
        return dataFeederTask.getCounter();
    }
}
