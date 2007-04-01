package org.openspaces.utest.enhancer.entry.index;

import org.openspaces.enhancer.entry.Entry;
import org.openspaces.enhancer.entry.RoutingIndex;
import org.openspaces.enhancer.entry.Indexed;

/**
 * @author kimchy
 */
@Entry
public class Data {

    String value1;

    @Indexed
    String value2;

    @RoutingIndex
    String value3;
}