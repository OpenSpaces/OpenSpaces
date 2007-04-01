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
