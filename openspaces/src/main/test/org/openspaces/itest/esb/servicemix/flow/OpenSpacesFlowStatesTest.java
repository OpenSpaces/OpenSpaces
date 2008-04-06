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
package org.openspaces.itest.esb.servicemix.flow;

import junit.framework.TestCase;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.esb.servicemix.flow.OpenSpacesFlow;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test several ways sending messages stateless and statefull  within clustered flows.
 *
 * @author yitzhaki
 */
public class OpenSpacesFlowStatesTest extends TestCase {

    protected JBIContainer jbi1;
    protected JBIContainer jbi2;
    protected GigaSpace gigaSpace;

    protected void setUp() throws Exception {
        gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./space").
                lookupGroups(System.getProperty("user.name")).space()).gigaSpace();
        jbi1 = createContainer("jbi1");
        jbi2 = createContainer("jbi2");
    }

    protected void tearDown() throws Exception {
        jbi1.shutDown();
        jbi2.shutDown();
        //let the jbi container time to shutdown.
        Thread.sleep(5000);
        gigaSpace.clean();
    }

    protected JBIContainer createContainer(String name) throws Exception {
        JBIContainer container = new JBIContainer();
        container.setName(name);
        OpenSpacesFlow osflow = new OpenSpacesFlow();
        osflow.setGigaSpace(gigaSpace);
        container.setFlowName("openspaces");
        container.setFlow(osflow);
        container.setUseMBeanServer(false);
        container.setEmbedded(true);
        container.init();
        container.start();
        return container;
    }

    protected StatelessEcho activateProvider(JBIContainer container, boolean stateless) throws Exception {
        StatelessEcho echo = new StatelessEcho(stateless);
        container.activateComponent(echo, "echo");
        return echo;
    }

    protected StatelessSender activateConsumer(JBIContainer container) throws Exception {
        StatelessSender sender = new StatelessSender();
        container.activateComponent(sender, "sender");
        return sender;
    }

    /**
     * Test case where the consumer is stateless.
     *
     * @throws Exception
     */
    public void testStatelessConsumer() throws Exception {
        activateProvider(jbi1, false);
        activateProvider(jbi2, false);
        StatelessSender sender1 = activateConsumer(jbi1);
        StatelessSender sender2 = activateConsumer(jbi2);

        sender1.sendMessages(100, true);

        int n1 = 0;
        int n2 = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            n1 = sender1.outIds.size();
            n2 = sender2.outIds.size();
            if (n1 + n2 == 100) {
                break;
            }
        }
        assertTrue(n1 != 0);
        assertTrue(n2 != 0);
        assertTrue(n1 + n2 == 100);

    }

    /**
     * Test case where the consumer is statefull.
     *
     * @throws Exception
     */
    public void testStatefullConsumer() throws Exception {
        activateProvider(jbi1, false);
        activateProvider(jbi2, false);
        StatelessSender sender1 = activateConsumer(jbi1);
        StatelessSender sender2 = activateConsumer(jbi2);

        sender1.sendMessages(100, false);

        int n1 = 0;
        int n2 = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            n1 = sender1.outIds.size();
            n2 = sender2.outIds.size();
            if (n1 + n2 == 100) {
                break;
            }
        }
        assertTrue(n1 != 0);
        assertTrue(n2 == 0);
        assertTrue(n1 + n2 == 100);
    }

    /**
     * Test case where the provider is stateless.
     *
     * @throws Exception
     */
    public void testStatelessProvider() throws Exception {
        StatelessEcho echo1 = activateProvider(jbi1, true);
        StatelessEcho echo2 = activateProvider(jbi2, true);
        StatelessSender sender1 = activateConsumer(jbi1);
        activateConsumer(jbi2);

        sender1.sendMessages(100, false);

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            if (echo1.doneIds.size() + echo2.doneIds.size() == 100) {
                break;
            }
        }
        assertTrue(echo1.doneIds.size() + echo2.doneIds.size() == 100);

        // Check that the echo1 component received
        // DONE status for exchanges it did not handle
        // the first time.
        // Do not bother testing for echo2, as it will
        // be automatically true.
        Set doneIds1 = new HashSet();
        doneIds1.addAll(echo1.doneIds);
        doneIds1.removeAll(echo1.inIds);
        assertTrue(doneIds1.size() > 0);
    }

    /**
     * Test case where the provider is statefull.
     *
     * @throws Exception
     */
    public void testStatefullProvider() throws Exception {
        StatelessEcho echo1 = activateProvider(jbi1, false);
        StatelessEcho echo2 = activateProvider(jbi2, false);
        StatelessSender sender1 = activateConsumer(jbi1);
        activateConsumer(jbi2);

        sender1.sendMessages(100, false);

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            if (echo1.doneIds.size() + echo2.doneIds.size() == 100) {
                break;
            }
        }
        assertTrue(echo1.doneIds.size() + echo2.doneIds.size() == 100);

        // Check that the echo1 component received
        // DONE status for exchanges it handle the first time.
        // Do not bother testing for echo2, as it will
        // be automatically true.
        Set doneIds1 = new HashSet();
        doneIds1.addAll(echo1.doneIds);
        doneIds1.removeAll(echo1.inIds);
        assertTrue(doneIds1.size() == 0);
    }

    public static class StatelessSender extends ComponentSupport implements MessageExchangeListener {

        public static final QName SERVICE = new QName("sender");
        public static final String ENDPOINT = "ep";

        List outIds = new CopyOnWriteArrayList();

        public StatelessSender() {
            super(SERVICE, ENDPOINT);
        }

        public void sendMessages(int nb, boolean stateless) throws Exception {
            for (int i = 0; i < nb; i++) {
                MessageExchangeFactory mef = getDeliveryChannel().createExchangeFactory();
                InOut me = mef.createInOutExchange();
                me.setService(new QName("echo"));
                if (stateless) {
                    me.setProperty(JbiConstants.STATELESS_CONSUMER, Boolean.TRUE);
                }
                me.setInMessage(me.createMessage());
                me.getInMessage().setContent(new StringSource("<hello/>"));
                getDeliveryChannel().send(me);

            }
        }

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            outIds.add(exchange.getExchangeId());
            done(exchange);
        }
    }

    public static class StatelessEcho extends ComponentSupport implements MessageExchangeListener {

        boolean stateless;
        List inIds = new CopyOnWriteArrayList();
        List doneIds = new CopyOnWriteArrayList();

        public StatelessEcho(boolean stateless) {
            setService(new QName("echo"));
            setEndpoint("ep");
            this.stateless = stateless;
        }

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            if (exchange.getStatus() == ExchangeStatus.DONE) {
                doneIds.add(exchange.getExchangeId());
            } else {
                inIds.add(exchange.getExchangeId());
                if (stateless) {
                    exchange.setProperty(JbiConstants.STATELESS_PROVIDER, Boolean.TRUE);
                }
                NormalizedMessage out = exchange.createMessage();
                out.setContent(new StringSource("<world/>"));
                answer(exchange, out);
            }
        }
    }

}
