/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.persistency.cassandra.meta.data;

import java.nio.ByteBuffer;

import org.openspaces.persistency.cassandra.meta.ColumnMetadata;
import org.openspaces.persistency.cassandra.meta.DynamicColumnMetadata;

/**
 * A data holder for a column data and metadata, used for both read and write operations.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class ColumnData {
    
    private final Object         value;
    private final ColumnMetadata columnMetadata;
    
    public ColumnData(Object value, ColumnMetadata columnMetadata) {
        this.value = value;
        this.columnMetadata = columnMetadata;
    }

    /**
     * @return Retures the value as read from cassandra. Might be in serialized form (i.e {@link ByteBuffer})
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return Retures the value as read from cassandra in a deserialized form.
     */
    public Object getDeserializedValue() {
        if (isValueDeserialized()) {
            return value;
        }
        
        return columnMetadata.getSerializer().fromByteBuffer((ByteBuffer) value);
    }
    
    /**
     * @return The matching column metadata
     */
    public ColumnMetadata getColumnMetadata() {
        return columnMetadata;
    }

    /**
     * @return true if this is a dynamic column.
     */
    public boolean isDynamicColumn() {
        return columnMetadata instanceof DynamicColumnMetadata;
    }

    private boolean isValueDeserialized() {
        return !(value instanceof ByteBuffer);
    }
    
}
