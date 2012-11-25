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
package org.openspaces.persistency.cassandra.meta;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.mapping.node.TypeNode;
import org.openspaces.persistency.cassandra.meta.mapping.node.TypeNodeContext;
import org.openspaces.persistency.cassandra.meta.mapping.node.VirtualEntryTopLevelTypeNode;


/**
 * A singleton instance of {@link ColumnFamilyMetadata} for the column family used internally on Cassandra
 * to store all introduced types as {@link ColumnFamilyMetadata} blobs.
 * @since 9.5
 * @author Dan Kilman
 */
public class ColumnFamilyMetadataMetadata extends ColumnFamilyMetadata {
    public static final ColumnFamilyMetadataMetadata INSTANCE         = new ColumnFamilyMetadataMetadata();

    public static final String                       NAME             = "__ColumnFamilyMetadata";
    public static final String                       KEY_NAME         = "columnFamilyName";
    public static final String                       BLOB_COLUMN_NAME = "metadata";
    
    private ColumnFamilyMetadataMetadata() {
        super(new VirtualEntryTopLevelTypeNode(NAME, 
                                               KEY_NAME, 
                                               String.class, 
                                               getSelfMetadataFields(), 
                                               new TypeNodeContext()), 
              null,
              new ColumnFamilyNameConverter() {
                public String toColumnFamilyName(String typeName) {
                    return typeName;
              }},
              null);
    }

    private static Map<String, TypeNode> getSelfMetadataFields() {
        Map<String, TypeNode> result = new HashMap<String, TypeNode>();
        result.put(BLOB_COLUMN_NAME, new TypedColumnMetadata(null,
                                                        BLOB_COLUMN_NAME, 
                                                        ColumnFamilyMetadata.class,
                                                        new TypeNodeContext(),
                                                        null));
        return result;
    }
    
}
