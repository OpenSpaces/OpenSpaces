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
package org.openspaces.utest.enhancer.entry.binary;

import junit.framework.TestCase;
import org.openspaces.enhancer.io.BinaryEntry;

/**
 * @author kimchy
 */
public class BinaryEntryEnhancerTests extends TestCase {


    public void testSimpleBinary() throws Exception {
        Data data = new Data();
        data.setValue("test");
        data.setIntValue(null);
        data.setIntValue2(2);
        ((BinaryEntry) data).pack();

        data.setValue(null);
        data.setIntValue(null);
        data.setIntValue2(null);
        ((BinaryEntry) data).unpack();
        assertEquals("test", data.getValue());
        assertNull(data.getIntValue());
        assertEquals(2, data.getIntValue2().intValue());
    }
}
