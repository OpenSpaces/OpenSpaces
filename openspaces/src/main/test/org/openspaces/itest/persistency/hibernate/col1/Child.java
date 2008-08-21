package org.openspaces.itest.persistency.hibernate.col1;

/**
 * @author kimchy
 */
public class Child {

    private String name;

    Child() {
    }

    public Child(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
