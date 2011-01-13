package org.openspaces.itest.persistency.support;

import java.util.Map;

import com.gigaspaces.datasource.BulkItem;

/**
 * @author kimchy
 */
public class MockBulkItem implements BulkItem {

    private Object item;

    private short operation;

    public MockBulkItem(Object item, short operation) {
        this.item = item;
        this.operation = operation;
    }

    public Object getItem() {
        return item;
    }

    public short getOperation() {
        return operation;
    }

    public String getTypeName() {
        return item.getClass().getName();
    }

    public String getIdPropertyName() {
        return null;
    }

    public Object getIdPropertyValue() {
        return null;
    }

    public Map<String, Object> getItemValues() {
        return null;
    }
}
