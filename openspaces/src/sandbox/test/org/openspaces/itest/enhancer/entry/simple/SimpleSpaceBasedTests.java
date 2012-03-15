/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.enhancer.entry.simple;

import junit.framework.TestCase;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceFinder;
import net.jini.core.lease.Lease;

/**
 * @author kimchy
 */
public class SimpleSpaceBasedTests extends TestCase {

    public void testSimpleSpaceOperation() throws Exception {
        IJSpace space = (IJSpace) SpaceFinder.find("/./space");
        Data data = new Data();
        data.hidden = 12;
        data.byteValue = 2;
        space.write(data, null, Lease.FOREVER);

        data = (Data) space.read(new Data(), null, 0);
        assertNull(data.hidden);
        assertEquals(2, data.byteValue.byteValue());
    }
}
