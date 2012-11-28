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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openspaces.persistency.cassandra.meta.ColumnFamilyMetadata;


/**
 * A data holder for holding the row columns and the matching column family metadata.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class ColumnFamilyRow {

    private final Map<String, ColumnData> columns        = new HashMap<String, ColumnData>();
    private final List<ColumnData>        dynamicColumns = new LinkedList<ColumnData>();
    private final ColumnFamilyMetadata    columnFamilyMetadata;
    private final Object                  keyValue;
    private final ColumnFamilyRowType     rowType;

    public ColumnFamilyRow(
            ColumnFamilyMetadata columnFamilyMetadata, 
            Object keyValue,
            ColumnFamilyRowType rowType) {
        this.columnFamilyMetadata = columnFamilyMetadata;
        this.keyValue = keyValue;
        this.rowType = rowType;
    }

    /**
     * @param columnData Add the column data to this row instance.
     */
    public void addColumnData(ColumnData columnData) {
        columns.put(columnData.getColumnMetadata().getFullName(), columnData);
        if (columnData.isDynamicColumn()) {
            dynamicColumns.add(columnData);
        }
    }

    /**
     * @param name The column name.
     * @return the {@link ColumnData} matching this name, null if not found.
     */
    public ColumnData getColumn(String name) {
        return columns.get(name);
    }

    /**
     * @return The dynamic columns that are part of this row.
     * when {@link #getRowType()} == Read, the columns are sorted by column name.
     */
    public List<ColumnData> getDynamicColumns() {
        return dynamicColumns;
    }
    
    /** 
     * @return The columns that are part of this row.
     */
    public Map<String, ColumnData> getColumns() {
        return columns;
    }

    /**
     * @return The matching column family metadata for this row.
     */
    public ColumnFamilyMetadata getColumnFamilyMetadata() {
        return columnFamilyMetadata;
    }

    /**
     * @return The key value of this row. 
     * If {@link #getRowType()} == Read, the key value might be in a serialized form (i.e. {@link ByteBuffer})
     */
    public Object getKeyValue() {
        return keyValue;
    }

    /**
     * @return The key value in a deserialized form.
     */
    public Object getDeserializedKeyValue() {
        if (isKeyDeserilized()) {
            return keyValue;
        }
        return columnFamilyMetadata.getKeySerializer().fromByteBuffer((ByteBuffer) keyValue);
    }
    
    private boolean isKeyDeserilized() {
        return !(keyValue instanceof ByteBuffer);
    }
    
    /**
     * @return The row type.
     */
    public ColumnFamilyRowType getRowType() {
        return rowType;
    }

    public static enum ColumnFamilyRowType {
        
        /**
         * Denotes a row that is part of a read operation context.
         */
        Read, 
        
        /**
         * Denotes a row that is part of a modifiying batch operation context.
         * This row is to be removed from cassandra.
         */
        Remove, 
        
        /**
         * Denotes a row that is part of a modifiying batch operation context.
         * This row is to be written to cassandra.
         */
        Write, 
        
        /**
         * Denotes a row that is part of a modifiying batch operation context.
         * This row is to be updated in cassandra with the matching row being 
         * removed first.
         */
        Update,

        /**
         * Denotes a row that is part of a modifiying batch operation context.
         * This row is to be updated in cassandra without removing the matching
         * row first.
         */
        PartialUpdate
        
    }
    
}
