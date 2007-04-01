package org.openspaces.utest.enhancer.entry.message;

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
        origMessage.setContent(new byte[]{(byte) 1, (byte) 2});

        Message newMessage = new Message();
        ExternalizableHelper.externalize((Externalizable) origMessage, (Externalizable) newMessage);

        assertEquals(1, origMessage.getValue());
        assertEquals(2, newMessage.getContent().length);
        assertEquals(1, newMessage.getContent()[0]);
        assertEquals(2, newMessage.getContent()[1]);
    }

    public void testZeroSimpleMessage() throws Exception {
        Message origMessage = new Message();
        origMessage.setValue(1);
        origMessage.setContent(new byte[0]);

        Message newMessage = new Message();
        ExternalizableHelper.externalize((Externalizable) origMessage, (Externalizable) newMessage);

        assertEquals(1, origMessage.getValue());
        assertEquals(0, newMessage.getContent().length);
    }
}
