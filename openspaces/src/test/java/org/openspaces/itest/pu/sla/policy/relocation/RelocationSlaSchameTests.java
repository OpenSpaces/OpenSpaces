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

package org.openspaces.itest.pu.sla.policy.relocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.pu.sla.RelocationPolicy;
import org.openspaces.pu.sla.SLA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/pu/sla/policy/relocation/relocation-policy.xml")
public class RelocationSlaSchameTests   { 
@Autowired
    protected ApplicationContext ac;

    public RelocationSlaSchameTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/policy/relocation/relocation-policy.xml"};
    }


     @Test public void testRelocationSchema() {
        SLA sla = (SLA) ac.getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getPolicy());
        assertTrue(sla.getPolicy() instanceof RelocationPolicy);
        RelocationPolicy relocationPolicy = (RelocationPolicy) sla.getPolicy();
        assertEquals("test", relocationPolicy.getMonitor());
        assertEquals(0.2, relocationPolicy.getLow(),0);
        assertEquals(0.8, relocationPolicy.getHigh(),0);
    }
}

