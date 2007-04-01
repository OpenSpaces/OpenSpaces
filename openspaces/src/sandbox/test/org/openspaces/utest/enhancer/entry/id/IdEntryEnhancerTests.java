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
