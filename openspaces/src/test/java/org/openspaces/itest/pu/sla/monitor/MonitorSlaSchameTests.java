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

package org.openspaces.itest.pu.sla.monitor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.monitor.BeanPropertyMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/pu/sla/monitor/monitor.xml")
public class MonitorSlaSchameTests   { 
@Autowired
    protected ApplicationContext ac;

    public MonitorSlaSchameTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/monitor/monitor.xml"};
    }

     @Test public void testMonitor() {
        SLA sla = (SLA) ac.getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getMonitors());
        assertEquals(1, sla.getMonitors().size());
        BeanPropertyMonitor monitor = (BeanPropertyMonitor) sla.getMonitors().get(0);
        assertEquals("test", monitor.getName());
        assertEquals("bean", monitor.getRef());
        assertEquals("prop", monitor.getPropertyName());
    }
}

