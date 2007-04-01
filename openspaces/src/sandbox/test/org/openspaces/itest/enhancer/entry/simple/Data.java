package org.openspaces.itest.enhancer.entry.simple;

import org.openspaces.enhancer.entry.Entry;

/**
 * @author kimchy
 */
@Entry
public class Data {

    transient Integer  hidden;

    Byte byteValue;
}