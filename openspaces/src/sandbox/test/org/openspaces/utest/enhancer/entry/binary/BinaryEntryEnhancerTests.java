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
