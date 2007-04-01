package org.openspaces.utest.enhancer.entry.simple.inheritance;

import org.openspaces.enhancer.entry.Entry;

/**
 * @author kimchy
 */
@Entry
public class A {

    private Integer value1;

    public Integer getValue1() {
        return value1;
    }

    public void setValue1(Integer value1) {
        this.value1 = value1;
    }
}
