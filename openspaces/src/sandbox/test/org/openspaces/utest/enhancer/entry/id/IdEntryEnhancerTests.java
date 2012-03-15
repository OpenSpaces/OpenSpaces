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
package org.openspaces.utest.enhancer.entry.id;

import com.j_spaces.core.client.EntryInfo;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author kimchy
 */
public class IdEntryEnhancerTests extends TestCase {

    public void testUID() throws Exception {
        Data data = new Data();
        data.id = "test";

        Method getUID = Data.class.getMethod("__getEntryInfo");
        EntryInfo entryInfo = (EntryInfo) getUID.invoke(data);
        assertEquals("test", entryInfo.m_UID);

        Method setUID = Data.class.getMethod("__setEntryInfo", EntryInfo.class);
        entryInfo = new EntryInfo("newtest", 0);
        setUID.invoke(data, entryInfo);
        assertEquals("newtest", data.id);
    }
}
