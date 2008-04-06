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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

/**
 * Test loading from configuration two servicemix instance (creatin cluster) and and sending sync messages between them.
 *
 * @author yitzhaki
 */
public class OpenSpacesFlowClusterConfigurationTests extends TestCase {

    private static transient Log log = LogFactory.getLog(OpenSpacesFlowClusterConfigurationTests.class);

    private SpringJBIContainer jbi;

    private AbstractXmlApplicationContext context;

    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("org/openspaces/itest/esb/servicemix/flow/broker.xml");
        jbi = (SpringJBIContainer) context.getBean("jbi");
        assertNotNull("JBI Container not found in spring!", jbi);
    }

    protected void tearDown() throws Exception {
        context.close();
    }

    public void test() throws Exception {
        AbstractXmlApplicationContext ctx = null;
        try {
            ctx = new ClassPathXmlApplicationContext("org/openspaces/itest/esb/servicemix/flow/client.xml");
            ServiceMixClient client = (ServiceMixClient) ctx.getBean("client");
            Thread.sleep(2000);
            InOut exchange = client.createInOutExchange();
            exchange.setService(new QName("http://www.habuma.com/foo", "sayhelloService"));
            NormalizedMessage in = exchange.getInMessage();
            in.setContent(new StringSource("<hello>Sending hello </hello>"));
            log.info("sending, exchange.status=" + exchange.getStatus());
            client.sendSync(exchange);
            assertNotNull(exchange.getOutMessage());
            log.info("got response, exchange.out=" + new SourceTransformer().toString(exchange.getOutMessage().getContent()));
            client.done(exchange);
            // Wait for done to be delivered
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
}
