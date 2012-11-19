/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.persistency.support;

import java.util.Map;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.sync.DataSyncOperationType;

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

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#getUid()
     */
    @Override
    public String getUid() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#getDataSyncOperationType()
     */
    @Override
    public DataSyncOperationType getDataSyncOperationType() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#getDataAsObject()
     */
    @Override
    public Object getDataAsObject() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#getDataAsDocument()
     */
    @Override
    public SpaceDocument getDataAsDocument() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#getTypeDescriptor()
     */
    @Override
    public SpaceTypeDescriptor getTypeDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#supportsGetTypeDescriptor()
     */
    @Override
    public boolean supportsGetTypeDescriptor() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#supportsDataAsObject()
     */
    @Override
    public boolean supportsDataAsObject() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.sync.DataSyncOperation#supportsDataAsDocument()
     */
    @Override
    public boolean supportsDataAsDocument() {
        // TODO Auto-generated method stub
        return false;
    }
}
