package org.openspaces.utest.enhancer.entry.simple.inheritance;

import org.openspaces.enhancer.entry.Entry;

/**
 * @author kimchy
 */
@Entry
public class B extends A {

    private Integer value2;

    public Integer getX() {
        return super.getValue1();
    }

    public Integer getValue2() {
        return value2;
    }

    public void setValue2(Integer value2) {
        this.value2 = value2;
    }
}