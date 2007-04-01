package org.openspaces.enhancer.entry.enhancedentry;

import junit.framework.TestCase;
import net.jini.core.entry.Entry;

/**
 * Tests that when implementing {@link org.openspaces.enhancer.support.EnhancedEntry}
 * we do not perform any more enhancements.
 *
 * @author kimchy
 */
public class EnhancedEntryEnhancerTests extends TestCase {

    public void testNotEnhancement() {
        Data data = new Data();
        assertFalse(data instanceof Entry);
    }
}
