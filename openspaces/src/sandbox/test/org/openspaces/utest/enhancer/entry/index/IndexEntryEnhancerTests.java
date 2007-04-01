package org.openspaces.utest.enhancer.entry.index;

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author kimchy
 */
public class IndexEntryEnhancerTests extends TestCase {

    public void testUIDGeneration() throws Exception {
        Data data = new Data();

        Method getIndexed = Data.class.getMethod("__getSpaceIndexedFields");
        String[] indexedFields = (String[]) getIndexed.invoke(data);
        assertEquals(2, indexedFields.length);
        assertEquals("value3", indexedFields[0]);
        assertEquals("value2", indexedFields[1]);
    }
}