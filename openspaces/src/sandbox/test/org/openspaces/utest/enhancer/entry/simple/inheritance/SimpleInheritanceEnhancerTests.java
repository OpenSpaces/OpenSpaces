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
package org.openspaces.utest.enhancer.entry.simple.inheritance;

import junit.framework.TestCase;
import org.openspaces.enhancer.support.ExternalizableHelper;

import java.io.Externalizable;

/**
 * @author kimchy
 */
public class SimpleInheritanceEnhancerTests extends TestCase {

    public void testSimpleInheritance() throws Exception {
        B oldB = new B();
        oldB.setValue1(1);
        oldB.setValue2(2);

        B newB = new B();
        ExternalizableHelper.externalize((Externalizable) oldB, (Externalizable) newB);

        assertEquals(1, newB.getValue1().intValue());
        assertEquals(2, newB.getValue2().intValue());
    }
}
