package org.openspaces.enhancer.message;

import junit.framework.TestCase;
import org.openspaces.enhancer.support.ExternalizableHelper;

import java.io.Externalizable;

/**
 * @author kimchy
 */
public class MessageEntryEnhancerTests extends TestCase {

    public void testSimpleMessage() throws Exception {
        Message origMessage = new Message();
        origMessage.setValue(1);
        origMessage.setContent(new byte[] {(byte) 1, (byte) 2});

        Message newMessage = new Message();
        ExternalizableHelper.externalize((Externalizable) origMessage, (Externalizable) newMessage);

        assertEquals(1, origMessage.getValue());
    }
}
