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
package org.openspaces.persistency.cassandra.meta.mapping.node;

import java.util.HashMap;
import java.util.Map;

import me.prettyprint.hector.api.Serializer;

import org.openspaces.persistency.cassandra.meta.DynamicColumnMetadata;
import org.openspaces.persistency.cassandra.meta.TypedColumnMetadata;
import org.openspaces.persistency.cassandra.meta.mapping.filter.DefaultFlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.mapping.filter.PojoTypeFlattenPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.types.SerializerProvider;
import org.openspaces.persistency.cassandra.meta.types.dynamic.DynamicPropertySerializer;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializerHectorSerializerAdapter;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceDocumentSupport;
import com.gigaspaces.metadata.SpacePropertyDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptor;

/**
 * @since 9.1.1
 * @author Dan Kilman
 */
public class TypeNodeIntrospector {
    
    private static final int                DEFAULT_MAX_NESTING_LEVEL = 10;

    private final CassandraDocumentObjectConverter documentConverter = new CassandraDocumentObjectConverter();
    
    private final ProcedureCache procedureCache = new ProcedureCache();
    
    // used internally to decide if an object is a valid pojo for flattening
    private final FlattenedPropertiesFilter pojoTypeFilter           = new PojoTypeFlattenPropertiesFilter();

    private final FlattenedPropertiesFilter flattenedPropertiesFilter;

    private final int maxNestingLevel;

    private final Serializer<Object> fixedPropertyValueSerializer;

    private final Serializer<Object> dynamicPropertyValueSerializer;
    
    public TypeNodeIntrospector(
            PropertyValueSerializer fixedPropertyValueSerializer, 
            PropertyValueSerializer dynamicPropertyValueSerializer, 
            FlattenedPropertiesFilter flattenedPropertiesFilter,
            int maxNestingLevel) {
        
        // if this is null it means we infer the serializer for type
        // using SerializerProvider
        if (fixedPropertyValueSerializer == null) {
            this.fixedPropertyValueSerializer = null;
        } else {
            this.fixedPropertyValueSerializer = new PropertyValueSerializerHectorSerializerAdapter(fixedPropertyValueSerializer);
        }
        
        if (dynamicPropertyValueSerializer == null) {
            this.dynamicPropertyValueSerializer = DynamicPropertySerializer.get();
        } else {
            this.dynamicPropertyValueSerializer = new PropertyValueSerializerHectorSerializerAdapter(dynamicPropertyValueSerializer);
        }
        
        if (flattenedPropertiesFilter == null) {
            this.flattenedPropertiesFilter = new DefaultFlattenedPropertiesFilter();
        } else {
            this.flattenedPropertiesFilter = flattenedPropertiesFilter;
        }

        if (maxNestingLevel <= 0) {
            this.maxNestingLevel = DEFAULT_MAX_NESTING_LEVEL;
        } else {
            this.maxNestingLevel = maxNestingLevel;
        }
    }

    public Serializer<Object> getDynamicPropertyValueSerializer() {
        return dynamicPropertyValueSerializer;
    }

    public Serializer<Object> getFixedPropertyValueSerializer() {
        return fixedPropertyValueSerializer;
    }
    
    public int getMaxNestingLevel() {
        return maxNestingLevel;
    }
    
    public TopLevelTypeNode introspectTypeDescriptor(SpaceTypeDescriptor typeDescriptor) {
        TypeNodeContext context = new TypeNodeContext(this, true);
        context.increaseNestingLevel();
        
        String typeName = typeDescriptor.getTypeName();
        String keyName = null;
        Class<?> keyType = null;
        Map<String, TypeNode> initialChildren = new HashMap<String, TypeNode>();
        for (int i = 0; i < typeDescriptor.getNumOfFixedProperties(); i++) {
            SpacePropertyDescriptor propertyDescriptor = typeDescriptor.getFixedProperty(i);
            String name = propertyDescriptor.getName();
            Class<?> type = propertyDescriptor.getType();
            if (typeDescriptor.getIdPropertyName().equals(name)) {
                keyName = name;
                keyType = type;
            } else {
                TypeNode typeNode = introspect(null /* top level node has no name */, name, type, context);
                
                // this means we just inspected a space document type
                // currently we ignore this during type declaration
                // and only create the child node on the first write operation
                if (typeNode == null)
                    continue;
                
                initialChildren.put(name, typeNode);
            }
        }
        
        return new SpaceDocumentTopLevelTypeNode(typeName,
                                        keyName,
                                        keyType,
                                        initialChildren,
                                        context);
    }
    
    public TypeNode introspect(String parentFullName, String name, Object value, TypeNodeContext context) {
        final boolean shouldFlatten = 
                !context.surpassedLastAllowedNestingLevel() &&
                flattenedPropertiesFilter.shouldFlatten(parentFullName,
                                                         name,
                                                         value.getClass(),
                                                         context.isDynamic());

        if (shouldFlatten && value instanceof SpaceDocument) {
            return introspectSpaceDocument(parentFullName,
                                          name,
                                          ((SpaceDocument) value).getTypeName(),
                                          context);
        } else if (shouldFlatten && pojoTypeFilter.shouldFlatten(parentFullName,
                                                               name,
                                                               value.getClass(),
                                                               context.isDynamic())) {
                return introspectPojo(parentFullName,
                                      name,
                                      value.getClass(),
                                      context);
        } else {
            return introspectLeafTypeNode(parentFullName, name, value.getClass(), context);
        }
    }

    public TypeNode introspect(String parentFullName, String name, Class<?> type, TypeNodeContext context)
    {
        final boolean shouldFlatten = 
                !context.surpassedLastAllowedNestingLevel() &&
                flattenedPropertiesFilter.shouldFlatten(parentFullName,
                                                         name,
                                                         type,
                                                         context.isDynamic());
        
        if (shouldFlatten && SpaceDocument.class.isAssignableFrom(type)) {
            return introspectSpaceDocument(parentFullName, name, type, context);
        } else if (shouldFlatten && pojoTypeFilter.shouldFlatten(parentFullName,
                                                               name,
                                                               type,
                                                               context.isDynamic())) {
                return introspectPojo(parentFullName, name, type, context);
        } else {
                return introspectLeafTypeNode(parentFullName,
                                              name,
                                              type,
                                              context);
        }
    }
    
    private TypeNode introspectSpaceDocument(String parentFullName, String name, Class<?> type, TypeNodeContext context) {
        // if a VirtualEntry type is found during fixed properties introspection a null value is returned
        // denoting this field/fixed property should be ignored. it will be created dynamically during a write
        // operation
        return null;
    }
    
    private TypeNode introspectSpaceDocument(String parentFullName, String name, String typeName, TypeNodeContext context) {
        return new SpaceDocumentTypeNode(typeName, parentFullName, name, null, context);
    }
    
    private TypeNode introspectPojo(String parentFullName, String name, Class<?> type, TypeNodeContext context) {
        return new PojoTypeNode(parentFullName, name, type, context);
    }
    
    private TypeNode introspectLeafTypeNode(String parentFullName, String name, Class<?> type, TypeNodeContext context) {
        if (context.isDynamic() && 
            context.isUseDynamicPropertySerializerForDynamicColumns()) {
            return new DynamicColumnMetadata(parentFullName,
                                             name,
                                             dynamicPropertyValueSerializer);
        } else {
            Serializer<Object> serializer = null;
            // primitive types for fixed properties columns are always serialized
            // using native serialization
            if (!SerializerProvider.isCommonJavaType(type)) {
                serializer = fixedPropertyValueSerializer;
            }
                
            return new TypedColumnMetadata(parentFullName,
                                           name,
                                           type,
                                           context,
                                           serializer);
        }
    }

    public Object convertFromSpaceDocumentIfNeeded(Object value, TypeNode typeNode) {
        if (!SerializerProvider.isCommonJavaType(value.getClass()) &&
            typeNode != null && !(typeNode instanceof SpaceDocumentTypeNode)) {
            value = documentConverter.fromDocumentIfNeeded(
                    value, 
                    SpaceDocumentSupport.CONVERT, 
                    typeNode.getType());
        }
        return value;
    }
    
    public ProcedureCache getProcedureCache() {
        return procedureCache;
    }

}
