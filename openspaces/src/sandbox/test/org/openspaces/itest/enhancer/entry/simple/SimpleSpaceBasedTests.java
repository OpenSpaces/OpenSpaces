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
