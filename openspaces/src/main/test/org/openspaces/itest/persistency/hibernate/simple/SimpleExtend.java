package org.openspaces.itest.persistency.hibernate.simple;

/**
 * @author kimchy
 */
public class SimpleExtend extends SimpleBase {

    private String valueExtend;

    public SimpleExtend() {
    }

    public SimpleExtend(Integer id, String value, String valueExtend) {
        super(id, value);
        this.valueExtend = valueExtend;
    }

    public String getValueExtend() {
        return valueExtend;
    }

    public void setValueExtend(String valueExtend) {
        this.valueExtend = valueExtend;
    }
}
