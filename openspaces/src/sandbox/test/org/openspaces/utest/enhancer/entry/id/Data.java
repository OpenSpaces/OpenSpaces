package org.openspaces.utest.enhancer.entry.id;

import org.openspaces.enhancer.entry.Entry;
import org.openspaces.enhancer.entry.EntryId;

/**
 * @author kimchy
 */
@Entry
public class Data {

    @EntryId
    String id;
}
