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
package org.openspaces.persistency.cassandra.archive;

import javax.annotation.PostConstruct;

import org.openspaces.archive.ArchiveOperationHandler;
import org.openspaces.core.GigaSpace;
import org.openspaces.persistency.cassandra.HectorCassandraClient;
import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.conversion.DefaultColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.mapping.DefaultSpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.meta.mapping.filter.DefaultFlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Dan Kilman
 * @since 9.1.1
 */
public class CassandraArchiveOperationHandler implements ArchiveOperationHandler {

    private static final int DEFAULT_MAX_NESTING_LEVEL = 10;
    
    //injected (required)
    private HectorCassandraClient hectorClient;
    private GigaSpace gigaSpace;
    
    //injected (overrides default value)
    private Integer maxNestingLevel;
    private PropertyValueSerializer fixedPropertyValueSerializer;
    private FlattenedPropertiesFilter flattenedPropertiesFilter;
    private ColumnFamilyNameConverter columnFamilyNameConverter;
    
    
    //helper object
    private DefaultSpaceDocumentColumnFamilyMapper mapper;

    @Required
    public void setHectorClient(HectorCassandraClient hectorClient) {
        this.hectorClient = hectorClient;
    }

    @Required
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public void setMaxNestingLevel(int maxNestingLevel) {
        this.maxNestingLevel = maxNestingLevel;
    }
    
    @PostConstruct
    public void afterPropertiesSet() {
        
        if (gigaSpace == null) {
            throw new IllegalArgumentException("gigaSpace cannot be null");
        }
        
        if (hectorClient == null) {
            throw new IllegalArgumentException("hectorClient cannot be null");
        }
        
        if (columnFamilyNameConverter == null) {
          //default value
            columnFamilyNameConverter = new DefaultColumnFamilyNameConverter();
        }
        
        if (maxNestingLevel == null) {
          //default value
            maxNestingLevel = DEFAULT_MAX_NESTING_LEVEL;
        }
        
        mapper = new DefaultSpaceDocumentColumnFamilyMapper(fixedPropertyValueSerializer,
                null,                                           
                flattenedPropertiesFilter, 
                columnFamilyNameConverter,
                maxNestingLevel);    
    }
    
    //TODO: implement
    @Override
    public void archive(Object... objects) {

    }

    //TODO: implement
    @Override
    public boolean supportsBatchArchiving() {
        return true;
    }
}
