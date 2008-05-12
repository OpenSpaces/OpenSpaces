package org.openspaces.itest.persistency.hibernate.simple;

/**
 * @author kimchy
 */
public class SimpleBase {

    private Integer id;

    private String value;

    public SimpleBase() {
    }

    public SimpleBase(Integer id, String value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
