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

package org.openspaces.itest.pu.sla.memberaliveindicator;

import org.openspaces.pu.sla.SLA;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class MemberAliveIndicatorSlaSchameTests extends AbstractDependencyInjectionSpringContextTests {

    public MemberAliveIndicatorSlaSchameTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/memberaliveindicator/memberaliveindicator.xml"};
    }


    public void testMemberAliveIndicatorSchema() {
        SLA sla = (SLA) getApplicationContext().getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getMemberAliveIndicator());
        assertEquals(1, sla.getMemberAliveIndicator().getInvocationDelay());
        assertEquals(2, sla.getMemberAliveIndicator().getRetryCount());
        assertEquals(3, sla.getMemberAliveIndicator().getRetryTimeout());
    }
}