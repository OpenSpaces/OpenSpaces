package org.openspaces.utest.enhancer.entry.binary;

import org.openspaces.enhancer.entry.Binary;
import org.openspaces.enhancer.entry.Entry;

/**
 * @author kimchy
 */
@Entry
public class Data {

    @Binary
    private String value;

    @Binary
    private Integer intValue;

    @Binary
    private Integer intValue2;

    public String getValue() throws Exception {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Integer getIntValue2() {
        return intValue2;
    }

    public void setIntValue2(Integer intValue2) {
        this.intValue2 = intValue2;
    }
}