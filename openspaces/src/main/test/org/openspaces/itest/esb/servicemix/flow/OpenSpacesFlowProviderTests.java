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
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.FlowProvider;
import org.openspaces.esb.servicemix.flow.OpenSpacesFlow;

/**
 * Check that the flow provider recognize the openspaces flow.
 *
 * @author yitzhaki
 */
public class OpenSpacesFlowProviderTests extends TestCase {

    public void testGetFlow() throws Exception {
        Flow flow = FlowProvider.getFlow("openspaces");
        assertTrue(flow instanceof OpenSpacesFlow);
    }

}