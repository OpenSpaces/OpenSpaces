package org.openspaces.itest.persistency.support;

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
}
