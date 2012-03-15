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
package org.openspaces.utest.enhancer.entry.simple;

import junit.framework.TestCase;
import net.jini.core.entry.Entry;
import org.openspaces.enhancer.support.ExternalizableHelper;

import java.io.Externalizable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author kimchy
 */
public class SimpleEntryEnhancerTests extends TestCase {

    public void testHasEntryInterface() throws Exception {
        assertTrue(Entry.class.isAssignableFrom(Data.class));
    }

    public void testHiddenAndPublicFields() throws Exception {
        assertTrue(Modifier.isPublic(Data.class.getField("intValue").getModifiers()));
        Field[] fields = Data.class.getFields();
        for (Field field : fields) {
            if (field.getName().equals("hidden")) {
                assertFalse(Modifier.isPublic(field.getModifiers()));
            }
        }
    }

    public void testSimpleInt() throws Exception {
        Data origData = new Data();
        origData.setIntValue(1);

        Data newData = new Data();
        ExternalizableHelper.externalize((Externalizable) origData, (Externalizable) newData);
        assertEquals(1, origData.getIntValue().intValue());
    }

    public void testCorrectMarshalling() throws Exception {
        Data origData = new Data();
        origData.setHidden(11);
        origData.setByteValue((byte) 1);
        origData.setBooleanValue(true);
        origData.setShortValue((short) 123);
        origData.setIntValue(10);
        origData.setFloatValue(12.5f);
        origData.setLongValue(12345l);
        origData.setDoubleValue(123.45);
        origData.setBytes(new byte[]{(byte) 1, (byte) 2});
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, 1, 1);
        origData.setDate(calendar.getTime());
        origData.setBigDecimal(BigDecimal.TEN);

        Data newData = new Data();
        ExternalizableHelper.externalize((Externalizable) origData, (Externalizable) newData);

        assertNull(newData.getHidden());
        assertEquals(1, newData.getByteValue().byteValue());
        assertTrue(newData.getBooleanValue());
        assertEquals(123, newData.getShortValue().shortValue());
        assertEquals(10, newData.getIntValue().intValue());
        assertEquals(12.5f, newData.getFloatValue());
        assertEquals(Long.valueOf(12345l), newData.getLongValue());
        assertEquals(123.45, newData.getDoubleValue());
        assertEquals(2, newData.getBytes().length);
        assertEquals(1, newData.getBytes()[0]);
        assertEquals(calendar.getTime(), newData.getDate());
        assertEquals(BigDecimal.TEN, newData.getBigDecimal());
    }


    public void testNullValues() throws Exception {
        Data origData = new Data();
        origData.setHidden(11);
        origData.setIntValue(12);

        Data newData = new Data();
        ExternalizableHelper.externalize((Externalizable) origData, (Externalizable) newData);

        assertNull(newData.getHidden());
        assertNull(newData.getByteValue());
        assertNull(newData.getShortValue());
        assertNull(newData.getBooleanValue());
        assertNull(newData.getDoubleValue());
        assertNull(newData.getFloatValue());
        assertEquals(12, newData.getIntValue().intValue());
    }
}
