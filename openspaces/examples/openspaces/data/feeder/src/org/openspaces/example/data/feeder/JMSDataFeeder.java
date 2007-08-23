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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Destination;
import javax.jms.Session;

/**
 * A JMS feeder bean started a scheduled task that writes Data objects to the space.
 * 
 * The bean's JMS ConnectionFactory is injected with a MessageConverter that
 * converts the JMS ObjectMessages to the Data objects before writing them to the space.
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

    // JMS Destination
    private Destination destination;
    
    // JMS ConnectionFactory
    private ConnectionFactory connectionFactory;
    
    // JMS Connection
    private Connection connection;
    
    // JMS Session
    private Session session;
    
    // JMS MessageProducer
    private MessageProducer messageProducer;

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
    
    /** Sets the JMS ConnectionFactory */
    public void setConnectionFactory(ConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }
    
    /** Sets the JMS ConnectionFactory */
    public void setDestination(Destination destination)
    {
        this.destination = destination;
    }
    
   
    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING FEEDER WITH CYCLE [" + defaultDelay + "]");
        if (instanceId != null) {
            // have a range of ids based on the instance id of the processing unit
            startIdFrom = instanceId * 10000000;
        }
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("MyQueue");
        messageProducer = session.createProducer(destination);
        executorService = Executors.newScheduledThreadPool(1);
        dataFeederTask = new JMSDataFeederTask();
        sf = executorService.scheduleAtFixedRate(dataFeederTask, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
    }

    public void destroy() throws Exception {
        sf.cancel(true);
        connection.close();
        sf = null;
        executorService.shutdown();
    }

    public class JMSDataFeederTask implements Runnable {

        private long counter;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                Data data = new Data((counter++ % numberOfTypes), "JMS_FEEDER " + Long.toString(time));
                data.setId(startIdFrom + counter);
                data.setProcessed(false);
                
                // send to space using JMS API
                Message m = session.createObjectMessage(data);
                System.out.println("--- SENDING with JMS: " + data);
                messageProducer.send(m);
                System.out.println("--- WROTE with JMS: " + data);
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
